# Commit Card

**Seu histórico de commits, transformado em portfólio.**

O **Commit Card** é uma aplicação que permite aos desenvolvedores transformarem seus históricos e repositórios do GitHub em um portfólio web e em um currículo em formato PDF de forma automática e sem complicações. 

🌐 **Acesse o site oficial:** [commitcard.com.br](https://commitcard.com.br/)

## 🚀 Como funciona?

Com apenas 3 passos simples, você tem seu portfólio pronto:
1. **Conecte seu GitHub:** Faça login e o Commit Card buscará automaticamente seus repositórios públicos usando a API do GitHub.
2. **Escolha os destaques:** Selecione os projetos dos quais você tem mais orgulho e deseja exibir no seu portfólio e currículo.
3. **Compartilhe:** Gere um link público exclusivo para colocar em sua bio ou LinkedIn, além de exportar um PDF pronto para enviar a recrutadores.

## ✨ Recursos Principais

- **Sincronização com o GitHub:** Puxa nome, bio, avatar e repositórios diretamente da API oficial de forma automática.
- **Currículo em PDF automático:** Gera um documento em PDF com foto, contato, resumo, experiências, formação e projetos de destaque (gerado no backend utilizando a biblioteca `OpenPDF`).
- **Link Compartilhável:** URL própria para divulgação do seu trabalho (ex: na bio do LinkedIn, e-mail).
- **Curadoria dos projetos:** Você tem o controle. Escolha exatamente quais repositórios entram no portfólio.
- **Certificados e Prêmios:** Adição de certificações e premiações direto no portfólio, com link para a credencial.

## 🛠️ Tecnologias Utilizadas

Este projeto foi desenvolvido utilizando as seguintes tecnologias:

- **Java 17**
- **Spring Boot**
- **Spring Security (OAuth2 Client)** para a autenticação e integração com a API do GitHub
- **Spring Data JPA** & **PostgreSQL** para persistência de dados
- **OpenPDF** para a geração dos currículos em formato PDF
- **Maven** para o gerenciamento de dependências
- **Lombok** para redução de código boilerplate (getters, setters, etc.)

## ⚙️ Como executar o projeto localmente

### Pré-requisitos
- [Java 17+](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
- [Maven](https://maven.apache.org/)
- [PostgreSQL](https://www.postgresql.org/)
- Chaves OAuth Application do GitHub (Client ID e Client Secret)

### Passos

1. Clone o repositório:
   ```bash
   git clone https://github.com/seu-usuario/commitcard.git
   cd commitcard/app
   ```

2. Configure o banco de dados PostgreSQL e as credenciais do GitHub nas variáveis de ambiente ou no arquivo `src/main/resources/application.properties`.

3. Execute a aplicação usando o wrapper do Maven:
   ```bash
   ./mvnw spring-boot:run
   ```

4. Acesse no navegador:
   ```
   http://localhost:8080
   ```

## 📄 Licença

Este projeto está sob a licença [MIT](LICENSE). Veja o arquivo `LICENSE` para mais detalhes.
