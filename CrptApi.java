import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.time.Duration;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;

@RestController
@RequestMapping("https://ismp.crpt.ru/api/v3/lk/documents")
public class CrptApi {
		
	private int MinuteInterval;
	private int requestLimit;
	private static int documentCounter;
	
	private Bucket bucket;
	
	public CrptApi(int timeInterval, int requestLimit) {
		this.MinuteInterval = timeInterval;
		this.requestLimit = requestLimit;
	}

	@PostMapping("/create")
	public synchronized void createDocument(Object document, String signature) throws FileNotFoundException, UnsupportedEncodingException {		
				
		Bandwidth limit = Bandwidth.classic(requestLimit, Refill.greedy(requestLimit, Duration.ofMinutes(MinuteInterval)));
        this.bucket = Bucket4j.builder()
            .addLimit(limit)
            .build();
		
        if (bucket.tryConsume(1)) {
        	documentCounter++;
        	PrintWriter writer = new PrintWriter("the-file-name"+documentCounter+".txt", "UTF-8");
			writer.println(signature);
			writer.println(document);
			writer.close();
            return;
        }else {
        	System.out.println("Too many requests");
        }
        
	}

}
