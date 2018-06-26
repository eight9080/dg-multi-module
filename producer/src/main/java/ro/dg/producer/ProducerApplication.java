package ro.dg.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

interface MessageChannels{
	@Output
	MessageChannel output();
}

@EnableBinding(MessageChannels.class)
@SpringBootApplication
@RestController
public class ProducerApplication {

	private final MessageChannels channels;

	@Autowired
	public ProducerApplication(MessageChannels channels) {
		this.channels = channels;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/greet/{name}")
	void greet(@PathVariable String name){
		final Message<String> message = MessageBuilder.withPayload(name).build();
		this.channels.output().send(message);
	}

	public static void main(String[] args) {
		SpringApplication.run(ProducerApplication.class, args);
	}
}
