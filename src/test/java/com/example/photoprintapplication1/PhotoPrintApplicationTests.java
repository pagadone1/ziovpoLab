package com.example.photoprintapplication1;

import com.example.photoprintapplication1.service.SignatureService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = PhotoprintApplication.class)
@ActiveProfiles("test")
class PhotoPrintApplicationTests {

	@MockBean
	private SignatureService signatureService;

	@Test
	void contextLoads() {
	}

}
