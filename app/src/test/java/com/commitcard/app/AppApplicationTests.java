package com.commitcard.app;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

// Esse é o teste padrão que o Spring Initializr já cria sozinho quando o projeto nasce —
// ele sobe a aplicação inteira, do jeito que estava configurada pra rodar de verdade
// (com Postgres e OAuth2 reais). Rodando fora do IntelliJ, faltam as variáveis de ambiente
// (DB_PASSWORD, GITHUB_OAUTH_CLIENT_ID etc.), então damos um banco H2 em memória e
// credenciais falsas de OAuth2 só pra esse teste — o objetivo aqui é só confirmar que a
// aplicação "liga" sem erro de configuração, não testar nada específico.
@SpringBootTest
@TestPropertySource(properties = {
		"spring.datasource.url=jdbc:h2:mem:contextloadtest;DB_CLOSE_DELAY=-1",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.hibernate.ddl-auto=update",
		"spring.security.oauth2.client.registration.github.client-id=id-de-teste",
		"spring.security.oauth2.client.registration.github.client-secret=segredo-de-teste"
})
class AppApplicationTests {

	@Test
	void contextLoads() {
	}

}