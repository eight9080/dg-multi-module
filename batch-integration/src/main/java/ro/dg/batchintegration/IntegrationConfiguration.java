package ro.dg.batchintegration;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.integration.launch.JobLaunchRequest;
import org.springframework.batch.integration.launch.JobLaunchingGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.integration.file.dsl.Files;
import org.springframework.integration.transformer.GenericTransformer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
public class IntegrationConfiguration {


    @Bean
    MessageChannel files(){
        return MessageChannels.direct().get();
    }


    @RestController
    public static class FileNameRestController{

        private final MessageChannel files;

        @Autowired
        public FileNameRestController(MessageChannel files) {
            this.files = files;
        }

        @RequestMapping(method = RequestMethod.GET, value = "/files")
        void triggerJobForFile(@RequestParam String file){
            Message<File> fileMessage = MessageBuilder.withPayload(new File(file)).build();
            this.files.send(fileMessage);
        }
    }

    @Bean
    IntegrationFlow batchJobFlow(Job job,
                                 JobLauncher launcher,
                                 MessageChannel files,
                                 JdbcTemplate jdbcTemplate){

        return IntegrationFlows.from(files)

         .transform((GenericTransformer<File, JobLaunchRequest>) source -> {
            final JobParameters jp = new JobParametersBuilder()
                    .addString("file", source.getAbsolutePath())
                    .toJobParameters();
            return new JobLaunchRequest(job, jp);
        })
                .handle(new JobLaunchingGateway(launcher))
                .handle(JobExecution.class, (payload, headers) -> {
                    System.out.println("job execution status: " + payload.getExitStatus().toString());
                    List<Person> personList = jdbcTemplate.query("select * from PEOPLE", (resultSet, i) -> new Person(resultSet.getString("first"),
                            resultSet.getString("last"),
                            resultSet.getString("email")));

                    personList.forEach(System.out::println);
                    return null;
                })
                .get();
    }

    @Bean
    IntegrationFlow incomingFiles(@Value("${HOME}/Desktop/in") File dir) {
        return IntegrationFlows.from(
                Files.inboundAdapter(dir)
                        .preventDuplicates(true)
                        .autoCreateDirectory(true),
                poller -> poller.poller(spec -> spec.fixedRate(1, TimeUnit.SECONDS)))
                .channel(this.files())
                .get();
    }

//    @Bean
//    IntegrationFlow incomingFiles(@Value("${HOME}/Desktop/in") File dir,
//                                  Job job,
//                                  JobLauncher launcher,
//                                  JdbcTemplate jdbcTemplate) {
//        return IntegrationFlows.from(
//                Files.inboundAdapter(dir)
//                        .preventDuplicates(true)
//                        .autoCreateDirectory(true),
//                                poller -> poller.poller(spec -> spec.fixedRate(1, TimeUnit.SECONDS)))
////                .handle(File.class, (payload, headers) -> {
////                    System.out.println("we have seen this file called "+payload.getAbsolutePath());
////                    return null;
////                })
//                .transform((GenericTransformer<File, JobLaunchRequest>) source -> {
//                    final JobParameters jp = new JobParametersBuilder()
//                            .addString("file", source.getAbsolutePath())
//                            .toJobParameters();
//                    return new JobLaunchRequest(job, jp);
//                })
//                .handle(new JobLaunchingGateway(launcher))
//                .handle(JobExecution.class, (payload, headers) -> {
//                    System.out.println("job execution status: " + payload.getExitStatus().toString());
//                    List<Person> personList = jdbcTemplate.query("select * from PEOPLE", (resultSet, i) -> new Person(resultSet.getString("first"),
//                            resultSet.getString("last"),
//                            resultSet.getString("email")));
//
//                    personList.forEach(System.out::println);
//                    return null;
//                })
//                .get();
//    }
}
