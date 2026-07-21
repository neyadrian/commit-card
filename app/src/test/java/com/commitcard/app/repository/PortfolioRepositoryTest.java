package com.commitcard.app.repository;

import com.commitcard.app.model.Project;
import com.commitcard.app.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

// @DataJpaTest sobe só a camada de banco (não o Spring inteiro), e usa um banco
// H2 em memória isolado — não toca no seu Postgres de verdade. Cada teste roda
// dentro de uma transação que é desfeita no final, então um teste nunca "suja"
// o próximo.
@DataJpaTest
class PortfolioRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Test
    void deveEncontrarUsuarioPeloGithubUsername() {
        User user = new User("neyadrian");
        user.setNome("Ney Adrian");
        userRepository.save(user);

        Optional<User> encontrado = userRepository.findByGithubUsername("neyadrian");

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getNome()).isEqualTo("Ney Adrian");
    }

    @Test
    void deveRetornarVazioQuandoUsuarioNaoExiste() {
        Optional<User> encontrado = userRepository.findByGithubUsername("usuario-que-nao-existe");

        assertThat(encontrado).isEmpty();
    }

    @Test
    void deveSalvarProjetosJuntoComOUsuarioGracasAoCascade() {
        User user = new User("neyadrian");
        Project projeto = new Project();
        projeto.setNomeRepositorio("commitcard");
        projeto.setUser(user);
        user.getProjetos().add(projeto);

        User salvo = userRepository.save(user);

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getProjetos()).hasSize(1);
        assertThat(salvo.getProjetos().get(0).getId()).isNotNull();
    }

    @Test
    void deveApagarProjetoDoBancoQuandoRemovidoDaListaGracasAoOrphanRemoval() {
        User user = new User("neyadrian");
        Project projeto = new Project();
        projeto.setNomeRepositorio("commitcard");
        projeto.setUser(user);
        user.getProjetos().add(projeto);
        User salvo = userRepository.saveAndFlush(user);
        Long idDoProjeto = salvo.getProjetos().get(0).getId();

        salvo.getProjetos().clear();
        userRepository.saveAndFlush(salvo);

        assertThat(projectRepository.findById(idDoProjeto)).isEmpty();
    }
}