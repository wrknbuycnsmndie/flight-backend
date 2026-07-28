package wrknbuycnsmndie.flight;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ValidationErrorsIntegrationTest {

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new UnexpectedErrorController())
            .setControllerAdvice(new wrknbuycnsmndie.flight.common.exception.GlobalExceptionHandler())
            .build();

    @Test
    void returnsSafeProblemDetailForUnexpectedErrors() throws Exception {
        mockMvc.perform(get("/api/test/unexpected").contextPath("/api"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Internal Server Error"))
                .andExpect(jsonPath("$.detail").value("An unexpected error occurred"))
                .andExpect(jsonPath("$.path").value("/api/test/unexpected"))
                .andExpect(jsonPath("$.exceptionMessage").doesNotExist());
    }

    @RestController
    static class UnexpectedErrorController {

        @GetMapping("/test/unexpected")
        void fail() {
            throw new IllegalStateException("secret implementation detail");
        }
    }
}
