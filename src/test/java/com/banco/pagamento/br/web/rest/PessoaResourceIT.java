package com.banco.pagamento.br.web.rest;

import static com.banco.pagamento.br.domain.PessoaAsserts.*;
import static com.banco.pagamento.br.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.banco.pagamento.br.IntegrationTest;
import com.banco.pagamento.br.domain.Pessoa;
import com.banco.pagamento.br.repository.PessoaRepository;
import com.banco.pagamento.br.service.dto.PessoaDTO;
import com.banco.pagamento.br.service.mapper.PessoaMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link PessoaResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class PessoaResourceIT {

    private static final byte[] DEFAULT_FOTO = TestUtil.createByteArray(1, "0");
    private static final byte[] UPDATED_FOTO = TestUtil.createByteArray(1, "1");
    private static final String DEFAULT_FOTO_CONTENT_TYPE = "image/jpg";
    private static final String UPDATED_FOTO_CONTENT_TYPE = "image/png";

    private static final String DEFAULT_NOME = "AAAAAAAAAA";
    private static final String UPDATED_NOME = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_DT_NASCIMENTO = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_DT_NASCIMENTO = LocalDate.now(ZoneId.systemDefault());
    private static final LocalDate SMALLER_DT_NASCIMENTO = LocalDate.ofEpochDay(-1L);

    private static final String DEFAULT_CPF = "458.255.26269";
    private static final String UPDATED_CPF = "67459641299";

    private static final String DEFAULT_EMAIL = "WV@Ok6.bjq1.nQ.aNN";
    private static final String UPDATED_EMAIL = "d-I2ey@Wyq.7l1D.rQ.nsYNHh.-D1u.H.Qk8";

    private static final String ENTITY_API_URL = "/api/pessoas";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private PessoaMapper pessoaMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restPessoaMockMvc;

    private Pessoa pessoa;

    private Pessoa insertedPessoa;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Pessoa createEntity() {
        return new Pessoa()
            .foto(DEFAULT_FOTO)
            .fotoContentType(DEFAULT_FOTO_CONTENT_TYPE)
            .nome(DEFAULT_NOME)
            .dtNascimento(DEFAULT_DT_NASCIMENTO)
            .cpf(DEFAULT_CPF)
            .email(DEFAULT_EMAIL);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Pessoa createUpdatedEntity() {
        return new Pessoa()
            .foto(UPDATED_FOTO)
            .fotoContentType(UPDATED_FOTO_CONTENT_TYPE)
            .nome(UPDATED_NOME)
            .dtNascimento(UPDATED_DT_NASCIMENTO)
            .cpf(UPDATED_CPF)
            .email(UPDATED_EMAIL);
    }

    @BeforeEach
    public void initTest() {
        pessoa = createEntity();
    }

    @AfterEach
    public void cleanup() {
        if (insertedPessoa != null) {
            pessoaRepository.delete(insertedPessoa);
            insertedPessoa = null;
        }
    }

    @Test
    @Transactional
    void createPessoa() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Pessoa
        PessoaDTO pessoaDTO = pessoaMapper.toDto(pessoa);
        var returnedPessoaDTO = om.readValue(
            restPessoaMockMvc
                .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(pessoaDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            PessoaDTO.class
        );

        // Validate the Pessoa in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedPessoa = pessoaMapper.toEntity(returnedPessoaDTO);
        assertPessoaUpdatableFieldsEquals(returnedPessoa, getPersistedPessoa(returnedPessoa));

        insertedPessoa = returnedPessoa;
    }

    @Test
    @Transactional
    void createPessoaWithExistingId() throws Exception {
        // Create the Pessoa with an existing ID
        pessoa.setId(1L);
        PessoaDTO pessoaDTO = pessoaMapper.toDto(pessoa);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restPessoaMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(pessoaDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Pessoa in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNomeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        pessoa.setNome(null);

        // Create the Pessoa, which fails.
        PessoaDTO pessoaDTO = pessoaMapper.toDto(pessoa);

        restPessoaMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(pessoaDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkDtNascimentoIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        pessoa.setDtNascimento(null);

        // Create the Pessoa, which fails.
        PessoaDTO pessoaDTO = pessoaMapper.toDto(pessoa);

        restPessoaMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(pessoaDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCpfIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        pessoa.setCpf(null);

        // Create the Pessoa, which fails.
        PessoaDTO pessoaDTO = pessoaMapper.toDto(pessoa);

        restPessoaMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(pessoaDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkEmailIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        pessoa.setEmail(null);

        // Create the Pessoa, which fails.
        PessoaDTO pessoaDTO = pessoaMapper.toDto(pessoa);

        restPessoaMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(pessoaDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllPessoas() throws Exception {
        // Initialize the database
        insertedPessoa = pessoaRepository.saveAndFlush(pessoa);

        // Get all the pessoaList
        restPessoaMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(pessoa.getId().intValue())))
            .andExpect(jsonPath("$.[*].fotoContentType").value(hasItem(DEFAULT_FOTO_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].foto").value(hasItem(Base64.getEncoder().encodeToString(DEFAULT_FOTO))))
            .andExpect(jsonPath("$.[*].nome").value(hasItem(DEFAULT_NOME)))
            .andExpect(jsonPath("$.[*].dtNascimento").value(hasItem(DEFAULT_DT_NASCIMENTO.toString())))
            .andExpect(jsonPath("$.[*].cpf").value(hasItem(DEFAULT_CPF)))
            .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL)));
    }

    @Test
    @Transactional
    void getPessoa() throws Exception {
        // Initialize the database
        insertedPessoa = pessoaRepository.saveAndFlush(pessoa);

        // Get the pessoa
        restPessoaMockMvc
            .perform(get(ENTITY_API_URL_ID, pessoa.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(pessoa.getId().intValue()))
            .andExpect(jsonPath("$.fotoContentType").value(DEFAULT_FOTO_CONTENT_TYPE))
            .andExpect(jsonPath("$.foto").value(Base64.getEncoder().encodeToString(DEFAULT_FOTO)))
            .andExpect(jsonPath("$.nome").value(DEFAULT_NOME))
            .andExpect(jsonPath("$.dtNascimento").value(DEFAULT_DT_NASCIMENTO.toString()))
            .andExpect(jsonPath("$.cpf").value(DEFAULT_CPF))
            .andExpect(jsonPath("$.email").value(DEFAULT_EMAIL));
    }

    @Test
    @Transactional
    void getPessoasByIdFiltering() throws Exception {
        // Initialize the database
        insertedPessoa = pessoaRepository.saveAndFlush(pessoa);

        Long id = pessoa.getId();

        defaultPessoaFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultPessoaFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultPessoaFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllPessoasByNomeIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedPessoa = pessoaRepository.saveAndFlush(pessoa);

        // Get all the pessoaList where nome equals to
        defaultPessoaFiltering("nome.equals=" + DEFAULT_NOME, "nome.equals=" + UPDATED_NOME);
    }

    @Test
    @Transactional
    void getAllPessoasByNomeIsInShouldWork() throws Exception {
        // Initialize the database
        insertedPessoa = pessoaRepository.saveAndFlush(pessoa);

        // Get all the pessoaList where nome in
        defaultPessoaFiltering("nome.in=" + DEFAULT_NOME + "," + UPDATED_NOME, "nome.in=" + UPDATED_NOME);
    }

    @Test
    @Transactional
    void getAllPessoasByNomeIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedPessoa = pessoaRepository.saveAndFlush(pessoa);

        // Get all the pessoaList where nome is not null
        defaultPessoaFiltering("nome.specified=true", "nome.specified=false");
    }

    @Test
    @Transactional
    void getAllPessoasByNomeContainsSomething() throws Exception {
        // Initialize the database
        insertedPessoa = pessoaRepository.saveAndFlush(pessoa);

        // Get all the pessoaList where nome contains
        defaultPessoaFiltering("nome.contains=" + DEFAULT_NOME, "nome.contains=" + UPDATED_NOME);
    }

    @Test
    @Transactional
    void getAllPessoasByNomeNotContainsSomething() throws Exception {
        // Initialize the database
        insertedPessoa = pessoaRepository.saveAndFlush(pessoa);

        // Get all the pessoaList where nome does not contain
        defaultPessoaFiltering("nome.doesNotContain=" + UPDATED_NOME, "nome.doesNotContain=" + DEFAULT_NOME);
    }

    @Test
    @Transactional
    void getAllPessoasByDtNascimentoIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedPessoa = pessoaRepository.saveAndFlush(pessoa);

        // Get all the pessoaList where dtNascimento equals to
        defaultPessoaFiltering("dtNascimento.equals=" + DEFAULT_DT_NASCIMENTO, "dtNascimento.equals=" + UPDATED_DT_NASCIMENTO);
    }

    @Test
    @Transactional
    void getAllPessoasByDtNascimentoIsInShouldWork() throws Exception {
        // Initialize the database
        insertedPessoa = pessoaRepository.saveAndFlush(pessoa);

        // Get all the pessoaList where dtNascimento in
        defaultPessoaFiltering(
            "dtNascimento.in=" + DEFAULT_DT_NASCIMENTO + "," + UPDATED_DT_NASCIMENTO,
            "dtNascimento.in=" + UPDATED_DT_NASCIMENTO
        );
    }

    @Test
    @Transactional
    void getAllPessoasByDtNascimentoIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedPessoa = pessoaRepository.saveAndFlush(pessoa);

        // Get all the pessoaList where dtNascimento is not null
        defaultPessoaFiltering("dtNascimento.specified=true", "dtNascimento.specified=false");
    }

    @Test
    @Transactional
    void getAllPessoasByDtNascimentoIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedPessoa = pessoaRepository.saveAndFlush(pessoa);

        // Get all the pessoaList where dtNascimento is greater than or equal to
        defaultPessoaFiltering(
            "dtNascimento.greaterThanOrEqual=" + DEFAULT_DT_NASCIMENTO,
            "dtNascimento.greaterThanOrEqual=" + UPDATED_DT_NASCIMENTO
        );
    }

    @Test
    @Transactional
    void getAllPessoasByDtNascimentoIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedPessoa = pessoaRepository.saveAndFlush(pessoa);

        // Get all the pessoaList where dtNascimento is less than or equal to
        defaultPessoaFiltering(
            "dtNascimento.lessThanOrEqual=" + DEFAULT_DT_NASCIMENTO,
            "dtNascimento.lessThanOrEqual=" + SMALLER_DT_NASCIMENTO
        );
    }

    @Test
    @Transactional
    void getAllPessoasByDtNascimentoIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedPessoa = pessoaRepository.saveAndFlush(pessoa);

        // Get all the pessoaList where dtNascimento is less than
        defaultPessoaFiltering("dtNascimento.lessThan=" + UPDATED_DT_NASCIMENTO, "dtNascimento.lessThan=" + DEFAULT_DT_NASCIMENTO);
    }

    @Test
    @Transactional
    void getAllPessoasByDtNascimentoIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedPessoa = pessoaRepository.saveAndFlush(pessoa);

        // Get all the pessoaList where dtNascimento is greater than
        defaultPessoaFiltering("dtNascimento.greaterThan=" + SMALLER_DT_NASCIMENTO, "dtNascimento.greaterThan=" + DEFAULT_DT_NASCIMENTO);
    }

    @Test
    @Transactional
    void getAllPessoasByCpfIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedPessoa = pessoaRepository.saveAndFlush(pessoa);

        // Get all the pessoaList where cpf equals to
        defaultPessoaFiltering("cpf.equals=" + DEFAULT_CPF, "cpf.equals=" + UPDATED_CPF);
    }

    @Test
    @Transactional
    void getAllPessoasByCpfIsInShouldWork() throws Exception {
        // Initialize the database
        insertedPessoa = pessoaRepository.saveAndFlush(pessoa);

        // Get all the pessoaList where cpf in
        defaultPessoaFiltering("cpf.in=" + DEFAULT_CPF + "," + UPDATED_CPF, "cpf.in=" + UPDATED_CPF);
    }

    @Test
    @Transactional
    void getAllPessoasByCpfIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedPessoa = pessoaRepository.saveAndFlush(pessoa);

        // Get all the pessoaList where cpf is not null
        defaultPessoaFiltering("cpf.specified=true", "cpf.specified=false");
    }

    @Test
    @Transactional
    void getAllPessoasByCpfContainsSomething() throws Exception {
        // Initialize the database
        insertedPessoa = pessoaRepository.saveAndFlush(pessoa);

        // Get all the pessoaList where cpf contains
        defaultPessoaFiltering("cpf.contains=" + DEFAULT_CPF, "cpf.contains=" + UPDATED_CPF);
    }

    @Test
    @Transactional
    void getAllPessoasByCpfNotContainsSomething() throws Exception {
        // Initialize the database
        insertedPessoa = pessoaRepository.saveAndFlush(pessoa);

        // Get all the pessoaList where cpf does not contain
        defaultPessoaFiltering("cpf.doesNotContain=" + UPDATED_CPF, "cpf.doesNotContain=" + DEFAULT_CPF);
    }

    @Test
    @Transactional
    void getAllPessoasByEmailIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedPessoa = pessoaRepository.saveAndFlush(pessoa);

        // Get all the pessoaList where email equals to
        defaultPessoaFiltering("email.equals=" + DEFAULT_EMAIL, "email.equals=" + UPDATED_EMAIL);
    }

    @Test
    @Transactional
    void getAllPessoasByEmailIsInShouldWork() throws Exception {
        // Initialize the database
        insertedPessoa = pessoaRepository.saveAndFlush(pessoa);

        // Get all the pessoaList where email in
        defaultPessoaFiltering("email.in=" + DEFAULT_EMAIL + "," + UPDATED_EMAIL, "email.in=" + UPDATED_EMAIL);
    }

    @Test
    @Transactional
    void getAllPessoasByEmailIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedPessoa = pessoaRepository.saveAndFlush(pessoa);

        // Get all the pessoaList where email is not null
        defaultPessoaFiltering("email.specified=true", "email.specified=false");
    }

    @Test
    @Transactional
    void getAllPessoasByEmailContainsSomething() throws Exception {
        // Initialize the database
        insertedPessoa = pessoaRepository.saveAndFlush(pessoa);

        // Get all the pessoaList where email contains
        defaultPessoaFiltering("email.contains=" + DEFAULT_EMAIL, "email.contains=" + UPDATED_EMAIL);
    }

    @Test
    @Transactional
    void getAllPessoasByEmailNotContainsSomething() throws Exception {
        // Initialize the database
        insertedPessoa = pessoaRepository.saveAndFlush(pessoa);

        // Get all the pessoaList where email does not contain
        defaultPessoaFiltering("email.doesNotContain=" + UPDATED_EMAIL, "email.doesNotContain=" + DEFAULT_EMAIL);
    }

    private void defaultPessoaFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultPessoaShouldBeFound(shouldBeFound);
        defaultPessoaShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultPessoaShouldBeFound(String filter) throws Exception {
        restPessoaMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(pessoa.getId().intValue())))
            .andExpect(jsonPath("$.[*].fotoContentType").value(hasItem(DEFAULT_FOTO_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].foto").value(hasItem(Base64.getEncoder().encodeToString(DEFAULT_FOTO))))
            .andExpect(jsonPath("$.[*].nome").value(hasItem(DEFAULT_NOME)))
            .andExpect(jsonPath("$.[*].dtNascimento").value(hasItem(DEFAULT_DT_NASCIMENTO.toString())))
            .andExpect(jsonPath("$.[*].cpf").value(hasItem(DEFAULT_CPF)))
            .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL)));

        // Check, that the count call also returns 1
        restPessoaMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultPessoaShouldNotBeFound(String filter) throws Exception {
        restPessoaMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restPessoaMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingPessoa() throws Exception {
        // Get the pessoa
        restPessoaMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingPessoa() throws Exception {
        // Initialize the database
    	pessoaRepository.saveAndFlush(pessoa);

        int databaseSizeBeforeUpdate = pessoaRepository.findAll().size();

        // Update the pessoa
        Pessoa updatedPessoa = pessoaRepository.findById(pessoa.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedPessoa are not directly saved in db
        em.detach(updatedPessoa);
        updatedPessoa.nome(UPDATED_NOME).dtNascimento(UPDATED_DT_NASCIMENTO);
        PessoaDTO pessoaDTO = pessoaMapper.toDto(updatedPessoa);

        restPessoaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, pessoaDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(pessoaDTO))
            )
            .andExpect(status().isOk());

        // Validate the Pessoa in the database
        List<Pessoa> pessoaList = pessoaRepository.findAll();
        assertThat(pessoaList).hasSize(databaseSizeBeforeUpdate);
        Pessoa testPessoa = pessoaList.get(pessoaList.size() - 1);
        assertThat(testPessoa.getNome()).isEqualTo(UPDATED_NOME);
        assertThat(testPessoa.getDtNascimento()).isEqualTo(UPDATED_DT_NASCIMENTO);
    }

    @Test
    @Transactional
    void putNonExistingPessoa() throws Exception {
    	int databaseSizeBeforeUpdate = pessoaRepository.findAll().size();
        pessoa.setId(longCount.incrementAndGet());

        // Create the Pessoa
        PessoaDTO pessoaDTO = pessoaMapper.toDto(pessoa);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPessoaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, pessoaDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(pessoaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Pessoa in the database
        List<Pessoa> pessoaList = pessoaRepository.findAll();
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchPessoa() throws Exception {
    	int databaseSizeBeforeUpdate = pessoaRepository.findAll().size();
        pessoa.setId(longCount.incrementAndGet());

        // Create the Pessoa
        PessoaDTO pessoaDTO = pessoaMapper.toDto(pessoa);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPessoaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(pessoaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Pessoa in the database
        List<Pessoa> pessoaList = pessoaRepository.findAll();
        assertThat(pessoaList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamPessoa() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        pessoa.setId(longCount.incrementAndGet());

        // Create the Pessoa
        PessoaDTO pessoaDTO = pessoaMapper.toDto(pessoa);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPessoaMockMvc
            .perform(put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(pessoaDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Pessoa in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdatePessoaWithPatch() throws Exception {
        // Initialize the database
        insertedPessoa = pessoaRepository.saveAndFlush(pessoa);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the pessoa using partial update
        Pessoa partialUpdatedPessoa = new Pessoa();
        partialUpdatedPessoa.setId(pessoa.getId());

        partialUpdatedPessoa.foto(UPDATED_FOTO).fotoContentType(UPDATED_FOTO_CONTENT_TYPE).nome(UPDATED_NOME).email(UPDATED_EMAIL);

        restPessoaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPessoa.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedPessoa))
            )
            .andExpect(status().isOk());

        // Validate the Pessoa in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPessoaUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedPessoa, pessoa), getPersistedPessoa(pessoa));
    }

    @Test
    @Transactional
    void fullUpdatePessoaWithPatch() throws Exception {
        // Initialize the database
        insertedPessoa = pessoaRepository.saveAndFlush(pessoa);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the pessoa using partial update
        Pessoa partialUpdatedPessoa = new Pessoa();
        partialUpdatedPessoa.setId(pessoa.getId());

        partialUpdatedPessoa
            .foto(UPDATED_FOTO)
            .fotoContentType(UPDATED_FOTO_CONTENT_TYPE)
            .nome(UPDATED_NOME)
            .dtNascimento(UPDATED_DT_NASCIMENTO)
            .cpf(UPDATED_CPF)
            .email(UPDATED_EMAIL);

        restPessoaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPessoa.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedPessoa))
            )
            .andExpect(status().isOk());

        // Validate the Pessoa in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPessoaUpdatableFieldsEquals(partialUpdatedPessoa, getPersistedPessoa(partialUpdatedPessoa));
    }

    @Test
    @Transactional
    void patchNonExistingPessoa() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        pessoa.setId(longCount.incrementAndGet());

        // Create the Pessoa
        PessoaDTO pessoaDTO = pessoaMapper.toDto(pessoa);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPessoaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, pessoaDTO.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(pessoaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Pessoa in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchPessoa() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        pessoa.setId(longCount.incrementAndGet());

        // Create the Pessoa
        PessoaDTO pessoaDTO = pessoaMapper.toDto(pessoa);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPessoaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(pessoaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Pessoa in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamPessoa() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        pessoa.setId(longCount.incrementAndGet());

        // Create the Pessoa
        PessoaDTO pessoaDTO = pessoaMapper.toDto(pessoa);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPessoaMockMvc
            .perform(
                patch(ENTITY_API_URL).with(csrf()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(pessoaDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Pessoa in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deletePessoa() throws Exception {
        // Initialize the database
        insertedPessoa = pessoaRepository.saveAndFlush(pessoa);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the pessoa
        restPessoaMockMvc
            .perform(delete(ENTITY_API_URL_ID, pessoa.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return pessoaRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected Pessoa getPersistedPessoa(Pessoa pessoa) {
        return pessoaRepository.findById(pessoa.getId()).orElseThrow();
    }

    protected void assertPersistedPessoaToMatchAllProperties(Pessoa expectedPessoa) {
        assertPessoaAllPropertiesEquals(expectedPessoa, getPersistedPessoa(expectedPessoa));
    }

    protected void assertPersistedPessoaToMatchUpdatableProperties(Pessoa expectedPessoa) {
        assertPessoaAllUpdatablePropertiesEquals(expectedPessoa, getPersistedPessoa(expectedPessoa));
    }
}
