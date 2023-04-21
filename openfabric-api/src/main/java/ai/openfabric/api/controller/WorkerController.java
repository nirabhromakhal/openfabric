package ai.openfabric.api.controller;

import ai.openfabric.api.model.Worker;
import ai.openfabric.api.repository.WorkerRepository;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.core.InvocationBuilder;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("${node.api.path}/worker")
public class WorkerController {

    private DockerClient dockerClient = null;

    @Autowired
    private WorkerRepository workerRepository;

    DockerClient getClient() {
        if (dockerClient == null) {
            DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                    .build();

            DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                    .dockerHost(config.getDockerHost())
                    .sslConfig(config.getSSLConfig())
                    .build();

            dockerClient = DockerClientImpl.getInstance(config, httpClient);
        }
        return dockerClient;
    }

    @PostMapping(path = "/hello")
    public @ResponseBody String hello(@RequestBody String name) {
        return "Hello!" + name;
    }

    @GetMapping(path = "/list")
    public @ResponseBody List<Worker> listWorkers(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "page_size", defaultValue = "10") int pageSize
    ) {
        List<Worker> allWorkers = (List<Worker>) workerRepository.findAll();

        int startIndex = page * pageSize;
        int endIndex = startIndex + pageSize;

        if (startIndex > allWorkers.size()) return new ArrayList<Worker>();
        if (endIndex > allWorkers.size()) endIndex = allWorkers.size();

        return allWorkers.subList(startIndex, endIndex);
    }

    @PostMapping(path = "/create")
    public @ResponseBody String createWorker(
            @RequestParam String name,
            @RequestParam String image,
            @RequestParam(name = "host_port", defaultValue = "0") int hostPort,
            @RequestParam(name = "container_port", defaultValue = "0") int containerPort
    ) {
        try {
            CreateContainerResponse container = null;
            if (hostPort != 0  &&  containerPort != 0) {
                container = getClient().createContainerCmd(image)
                        .withName(name)
                        .withExposedPorts(ExposedPort.tcp(containerPort))   // Expose container post
                        .withHostConfig(HostConfig.newHostConfig()
                                .withPortBindings(PortBinding.parse(hostPort + ":" + containerPort)))   // Map host port to container port
                        .exec();
            }
            else {
                container = getClient().createContainerCmd(image)
                        .withName(name)
                        .exec();
            }

            // Create worker in DB
            InspectContainerResponse containerDetails = getClient().inspectContainerCmd(container.getId()).exec();
            String command = containerDetails.getConfig().getCmd() == null ? "" : String.join(" ", containerDetails.getConfig().getCmd());
            Worker worker = new Worker(name, container.getId(), command, image);
            if (hostPort != 0  &&  containerPort != 0) {
                worker.setHostPort(String.valueOf(hostPort));
                worker.setContainerPort(String.valueOf(containerPort));
            }
            worker.setStatus(containerDetails.getState().getStatus());

            workerRepository.save(worker);
            return "Success";
        }
        catch (Exception e) {
            return "Failure: " + e;
        }
    }


    @PostMapping(path = "/start")
    public @ResponseBody String startWorker(
            @RequestParam String name
    ) {
        try {
            getClient().startContainerCmd(name).exec();

            Worker worker = workerRepository.findByName(name);
            InspectContainerResponse containerDetails = getClient().inspectContainerCmd(name).exec();
            worker.setStatus(containerDetails.getState().getStatus());

            workerRepository.save(worker);
        }
        catch (Exception e) {
            return "Failure: " + e;
        }
        return "Success";
    }

    @PostMapping(path = "/stop")
    public @ResponseBody String stopWorker(
            @RequestParam String name
    ) {
        try {
            getClient().stopContainerCmd(name).exec();

            Worker worker = workerRepository.findByName(name);
            InspectContainerResponse containerDetails = getClient().inspectContainerCmd(name).exec();
            worker.setStatus(containerDetails.getState().getStatus());

            workerRepository.save(worker);
        }
        catch (Exception e) {
            return "Failure: " + e;
        }
        return "Success";
    }

    @GetMapping(path = "/info")
    public @ResponseBody Worker getWorkerInfo(
            @RequestParam String name
    ) {
        try {
            return workerRepository.findByName(name);
        }
        catch (Exception e) {
            return null;
        }
    }

    @GetMapping(path = "/stats")
    public @ResponseBody Statistics getWorkerStats(
            @RequestParam String name
    ) {
        try {
            InvocationBuilder.AsyncResultCallback<Statistics> callback = new InvocationBuilder.AsyncResultCallback<>();
            getClient().statsCmd(name).exec(callback);
            return callback.awaitResult();
        }
        catch (Exception e) {
            return null;
        }
    }

}
