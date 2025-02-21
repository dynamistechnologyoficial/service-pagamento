package com.banco.pagamento.br;

import com.banco.pagamento.br.config.AsyncSyncConfiguration;
import com.banco.pagamento.br.config.EmbeddedSQL;
import com.banco.pagamento.br.config.JacksonConfiguration;
import com.banco.pagamento.br.config.TestSecurityConfiguration;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Base composite annotation for integration tests.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(
    classes = { ServicePagamentoApp.class, JacksonConfiguration.class, AsyncSyncConfiguration.class, TestSecurityConfiguration.class }
)
@EmbeddedSQL
public @interface IntegrationTest {
}
