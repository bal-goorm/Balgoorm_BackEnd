package com.balgoorm.balgoorm_backend.ide.service;

import com.balgoorm.balgoorm_backend.ide.model.dto.request.CodeRunRequest;
import com.balgoorm.balgoorm_backend.ide.model.dto.response.CodeRunResponse;
import com.balgoorm.balgoorm_backend.ide.model.enums.LanguageType;
import com.balgoorm.balgoorm_backend.quiz.model.entity.Quiz;
import com.balgoorm.balgoorm_backend.quiz.model.entity.SolvedQuiz;
import com.balgoorm.balgoorm_backend.quiz.model.entity.SubmitRecord;
import com.balgoorm.balgoorm_backend.quiz.repository.QuizRepository;
import com.balgoorm.balgoorm_backend.quiz.repository.SolvedQuizRepository;
import com.balgoorm.balgoorm_backend.quiz.repository.SubmitRecordRepository;
import com.balgoorm.balgoorm_backend.user.model.entity.User;
import com.balgoorm.balgoorm_backend.user.repository.UserRepository;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdeServiceImpl implements IdeService {

    private final DockerClient dockerClient;
    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final SubmitRecordRepository submitRecordRepository;
    private final SolvedQuizRepository solvedQuizRepository;

    /**
     * docker Container 의 containerId 를 저장할 HashMap;
     */
    static Map<String, String> containers = new HashMap<>();

    /**
     * 어플리케이션 시작과 동시에 미리 사용할 dockerContainer 생성
     * @throws IOException
     */
    @PostConstruct
    public void createContainers() throws IOException {
        String[] images = {"openjdk-with-time", "gcc-with-time", "python-with-time"};
        String[] dockerfiles = {"Dockerfile-java", "Dockerfile-cpp","Dockerfile-python"};
        LanguageType[] languages = {LanguageType.JAVA, LanguageType.CPP, LanguageType.PYTHON};
        String baseDirPath = "src/main/resources/dockerfile/";

        // 이미지를 빌드하는 로직
        for(int i = 0; i < dockerfiles.length; i++){
            String dockerfilePath = baseDirPath + dockerfiles[i];
            String imageName = images[i];

            String imageId = dockerClient.buildImageCmd(new File(dockerfilePath))
                    .withTags(new HashSet<>(java.util.Collections.singletonList(imageName + ":latest")))
                    .exec(new BuildImageResultCallback())
                    .awaitImageId();

            System.out.println(imageName + ":latest 이미지가 성공적으로 빌드되었습니다: " + imageId);

        }

        // 컨테이너를 생성하는 로직
        for (int i = 0; i < images.length; i++) {
            String projectDir = System.getProperty("user.dir") + "/code/" + getFileExtension(languages[i]);
            Files.createDirectories(Paths.get(projectDir));

            CreateContainerResponse container = dockerClient.createContainerCmd(images[i])
                    .withHostConfig(new HostConfig().withBinds(new Bind(projectDir, new Volume("/src"))))
                    .withCmd("/bin/sh", "-c", "while :; do sleep 1; done")
                    .withWorkingDir("/src")
                    .exec();
            log.info(getFileExtension(languages[i]) + " , " + container.getId());
            containers.put(getFileExtension(languages[i]), container.getId());
        }
    }

    /**
     * 어플리케이션이 종료될 때 사용중이였던 컨테이너를 모두 종료시킴
     */
    @PreDestroy
    public void destroyContainers() {
        for (Map.Entry<String, String> entry : containers.entrySet()) {
            try {
                dockerClient.stopContainerCmd(entry.getValue()).exec();
            } catch (Exception e) {
                System.out.println("Container is already stopped");
            }
            dockerClient.removeContainerCmd(entry.getValue()).exec();
        }
    }

    /**
     * 실제 코드를 실행시키는 메소드
     * @param executeRequest
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    @Override
    @Transactional
    public CodeRunResponse codeRun(CodeRunRequest executeRequest) throws IOException, InterruptedException {

        CodeRunResponse codeRunResponse = new CodeRunResponse();

        String compileCommand = "";
        String runCommand;
        String fileExtension = getFileExtension(executeRequest.getLanguage());
        // UUID 를 통해 고유한 디렉토리 명 생성
        String uniqueId = UUID.randomUUID().toString();
        String tempDirHost = System.getProperty("user.dir") + "/code/" + fileExtension + "/" + uniqueId;
        String tempDirContainer = "/src/" + fileExtension + "/" + uniqueId;

        // 고유한 디렉토리 생성
        Files.createDirectories(Paths.get(tempDirHost));
        log.info("Host temp dir: {}", tempDirHost);
        log.info("Container temp dir: {}", tempDirContainer);

        // 언어에 따른 명령어 설정
        switch (executeRequest.getLanguage()) {
            case JAVA:
                compileCommand = "javac Main.java";
                runCommand = "java Main";
                break;
            case CPP:
                compileCommand = "g++ -o main main.cpp";
                runCommand = "./main";
                break;
            case PYTHON:
                runCommand = "python main.py";
                break;
            default:
                throw new IllegalArgumentException("Unsupported language: " + executeRequest.getLanguage());
        }

        String filePath = tempDirHost + "/main." + fileExtension;
        Files.write(Paths.get(filePath), executeRequest.getCode().getBytes());

        String containerId = containers.get(fileExtension);
        log.info("container id : {}", containerId);

        InspectContainerResponse containerResponse = dockerClient.inspectContainerCmd(containerId).exec();
        if (!containerResponse.getState().getRunning()) {
            log.warn("container is not running");
            dockerClient.startContainerCmd(containerId).exec();
        }

        // 컨테이너 내부 디렉토리 생성
        ExecCreateCmdResponse mkdirCmdResponse = dockerClient.execCreateCmd(containerId)
                .withCmd("/bin/sh", "-c", "mkdir -p " + tempDirContainer)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .exec();

        dockerClient.execStartCmd(mkdirCmdResponse.getId())
                .exec(new ExecStartResultCallback(System.out, System.err))
                .awaitCompletion();

        // 호스트의 파일을 컨테이너 내부로 복사
        dockerClient.copyArchiveToContainerCmd(containerId)
                .withHostResource(tempDirHost + "/.")
                .withRemotePath(tempDirContainer)
                .exec();

        if (!compileCommand.trim().isEmpty()) {
            ExecCreateCmdResponse compileCmdResponse = dockerClient.execCreateCmd(containerId)
                    .withCmd("/bin/sh", "-c", "cd " + tempDirContainer + " && " + compileCommand)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .exec();

            ByteArrayOutputStream compileOutputStream = new ByteArrayOutputStream();
            ByteArrayOutputStream compileErrorStream = new ByteArrayOutputStream();

            boolean compileTimeout = execCommandWithTimeout(compileCmdResponse.getId(), compileOutputStream, compileErrorStream, 10, TimeUnit.SECONDS);

            if (compileTimeout) {
                codeRunResponse.setErrorMessage("Compilation timed out");
                cleanup(tempDirHost, containerId, tempDirContainer);
                return codeRunResponse;
            }

            String compileStderr = compileErrorStream.toString();

            if (!compileStderr.trim().isEmpty()) {
                log.info(compileStderr);
                codeRunResponse.setErrorMessage(compileStderr);
                cleanup(tempDirHost, containerId, tempDirContainer);
                return codeRunResponse;
            }
        }

        // 코드의 실행시간을 체크하기 위한 time 명령어 추가
        String timeCommand = "/usr/bin/time -v " + runCommand;

        ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                .withCmd("/bin/sh", "-c", "cd " + tempDirContainer + " && " + timeCommand)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .exec();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

        boolean executionTimeout = execCommandWithTimeout(execCreateCmdResponse.getId(), outputStream, errorStream, 5, TimeUnit.SECONDS);

        if (executionTimeout) {
            codeRunResponse.setErrorMessage("실행시간이 초과되었습니다...");
            stopContainer(containerId);
            cleanup(tempDirHost, containerId, tempDirContainer);
            return codeRunResponse;
        }

        String stdout = outputStream.toString();
        String stderr = errorStream.toString();

        String[] lines = stderr.split("\\r?\\n");

        // 파이썬은 실행시점에 에러가 발생할 수 있기 때문에 이를 확인한다
        if (!lines[0].split(":")[0].trim().equals("Command being timed")) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < lines.length; i++) {
                if (lines[i].split(":")[0].trim().equals("Command being timed")) {
                    break;
                }
                sb.append(lines[i]).append("\n");
            }
            codeRunResponse.setErrorMessage(sb.toString());
            cleanup(tempDirHost, containerId, tempDirContainer);
            return codeRunResponse;
        }

        String runTime = "";
        String memoryUsage = "";
        for (String line : lines) {
            if (line.contains("Elapsed (wall clock) time")) {
                runTime = convertToMilliseconds(line.split(":")[4].trim()+":"+line.split(":")[5].trim());
                codeRunResponse.setRunTime(runTime);
            }
            if (line.contains("Maximum resident set size")) {
                memoryUsage = line.split(":")[1].trim();
                codeRunResponse.setMemoryUsage(memoryUsage);
            }
        }



        String quizAnswer = getQuizAnswer(executeRequest.getQuizId());

        codeRunResponse.setResult(stdout);
        codeRunResponse.setAnswer(quizAnswer);
        boolean isCorrect = compareOutput(quizAnswer, stdout);
        codeRunResponse.setCorrect(isCorrect);

        saveSubmitRecord(executeRequest, isCorrect, runTime, memoryUsage);

        // 만약 정답이라면 해결 문제에 추가
        if (isCorrect) {
            Optional<SolvedQuiz> findSolvedQuiz = solvedQuizRepository.findByQuizIdAndUserId(executeRequest.getQuizId(), executeRequest.getUserId());
            if(!findSolvedQuiz.isPresent()) {
                SolvedQuiz solvedQuiz = SolvedQuiz.builder()
                        .quizId(executeRequest.getQuizId())
                        .userId(executeRequest.getUserId())
                        .solvedAt(LocalDateTime.now())
                        .build();
                solvedQuizRepository.save(solvedQuiz);
                // 한번 정답을 맞춘 사람의 제출기록과 정답 제출수는 기록하지 않음
                quizInfoUpdate(executeRequest.getQuizId(), isCorrect);
            }
        }

        cleanup(tempDirHost, containerId, tempDirContainer);

        return codeRunResponse;
    }

    /**
     * 답인 제출 기록을 저장하는 메소드
     * @param executeRequest
     * @param isCorrect
     * @param runTime
     * @param memoryUsage
     */
    private void saveSubmitRecord(CodeRunRequest executeRequest, boolean isCorrect, String runTime, String memoryUsage) {
        Optional<User> user = userRepository.findById(executeRequest.getUserId());
        Optional<Quiz> quiz = quizRepository.findById(executeRequest.getQuizId());

        if (!user.isPresent()) {
            throw new RuntimeException("user not found");
        }

        if (!quiz.isPresent()) {
            throw new RuntimeException("quiz not found");
        }

        SubmitRecord submitRecord = SubmitRecord.builder()
                .user(user.get())
                .quiz(quiz.get())
                .submitDate(LocalDateTime.now())
                .submitCode(executeRequest.getCode())
                .isSuccess(isCorrect)
                .executionTime(runTime)
                .memoryUsage(memoryUsage)
                .languageType(executeRequest.getLanguage())
                .build();

        submitRecordRepository.save(submitRecord);
    }

    /**
     * docker container 에 코드를 실행시켰을 때 일정시간 이상 응답되지 않으면 에러를 내기 위한 메소드
     * @param execId
     * @param outputStream
     * @param errorStream
     * @param timeout
     * @param unit
     * @return
     */
    private boolean execCommandWithTimeout(String execId, ByteArrayOutputStream outputStream, ByteArrayOutputStream errorStream, long timeout, TimeUnit unit) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<?> future = executor.submit(() -> {
            try {
                dockerClient.execStartCmd(execId)
                        .exec(new ExecStartResultCallback(outputStream, errorStream))
                        .awaitCompletion();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        try {
            future.get(timeout, unit);
            return false;
        } catch (TimeoutException e) {
            future.cancel(true);
            return true;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Command execution failed", e);
        } finally {
            executor.shutdown();
        }
    }

    /**
     * 응답시간이 초과되었을 때 컨테이너를 멈추기 위한 메소드
     * @param containerId
     */
    private void stopContainer(String containerId) {
        try {
            dockerClient.stopContainerCmd(containerId).exec();
        } catch (Exception e) {
            log.error("Failed to stop and remove container: " + containerId, e);
        }
    }

    /**
     * 퀴즈의 정보를 업데이트하기 위한 메소드
     * @param quizId
     * @param isCorrect
     */
    public void quizInfoUpdate(Long quizId, boolean isCorrect) {
        Optional<Quiz> findById = quizRepository.findById(quizId);

        if (!findById.isPresent()) {
            throw new RuntimeException("quiz not found");
        }

        int subCnt = findById.get().getSubmitCnt();
        findById.get().setSubmitCnt(subCnt + 1);
        if (isCorrect) {
            int corCnt = findById.get().getCorrectCnt();
            findById.get().setCorrectCnt(corCnt + 1);
        }
    }

    /**
     * 퀴즈 정답 비교하기위한 메소드
     * @param quizAnswer
     * @param stdout
     * @return
     */
    private boolean compareOutput(String quizAnswer, String stdout) {
        return quizAnswer.equals(stdout);
    }

    /**
     * 퀴즈의 정답을 가져오기위한 메소드
     * @param quizId
     * @return
     */
    private String getQuizAnswer(Long quizId) {
        Optional<Quiz> findById = quizRepository.findById(quizId);

        if (!findById.isPresent()) {
            throw new RuntimeException("Quiz not found");
        }

        return findById.get().getQuizAnswer().replace("\\n", "\n");
    }

    /**
     * 각 언어별로 파일의 확장자를 가져오기 위한 메소드
     * @param language
     * @return
     */
    private String getFileExtension(LanguageType language) {
        switch (language) {
            case JAVA:
                return "java";
            case CPP:
                return "cpp";
            case PYTHON:
                return "py";
            default:
                throw new IllegalArgumentException("Unsupported language: " + language);
        }
    }

    /**
     * hh:mm:ss 를 milliseconds 로 바꾸기 위한 메소드
     * @param timeStr
     * @return
     */
    private String convertToMilliseconds(String timeStr) {
        String[] parts = timeStr.split(":");
        long milliseconds = 0;
        if (parts.length == 2) {
            milliseconds += Integer.parseInt(parts[0]) * 60 * 1000;
            milliseconds += (long) (Double.parseDouble(parts[1]) * 1000);
        } else if (parts.length == 3) {
            milliseconds += Integer.parseInt(parts[0]) * 60 * 60 * 1000;
            milliseconds += Integer.parseInt(parts[1]) * 60 * 1000;
            milliseconds += (long) (Double.parseDouble(parts[2]) * 1000);
        }
        return String.valueOf(milliseconds);
    }

    /**
     * 임시로 만들었던 디렉토리를 전부 삭제하기 위한 메소드 ( 도커 컨테이너 내부 )s
     * @param tempDirHost
     * @param containerId
     * @param tempDirContainer
     */
    private void cleanup(String tempDirHost, String containerId, String tempDirContainer) {
        // 호스트에서 디렉토리 삭제
        deleteDirectory(new File(tempDirHost));
        // 컨테이너 내부의 파일 삭제
        try {
            ExecCreateCmdResponse deleteCmdResponse = dockerClient.execCreateCmd(containerId)
                    .withCmd("/bin/sh", "-c", "rm -rf " + tempDirContainer)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .exec();
            dockerClient.execStartCmd(deleteCmdResponse.getId())
                    .exec(new ExecStartResultCallback(System.out, System.err))
                    .awaitCompletion();
        } catch (InterruptedException e) {
            log.error("Failed to delete directory inside container", e);
        }
    }

    /**
     * 디렉토리 삭제를 위한 메소드 ( 호스트 )
     * @param directory
     */
    private void deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        directory.delete();
    }
}
