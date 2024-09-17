package example;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class CsrfAdvice {

	@ModelAttribute
	public void writeHeader(CsrfToken token, HttpServletResponse response) {
		response.addHeader(token.getHeaderName(), token.getToken());
	}

}
