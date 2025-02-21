package com.banco.pagamento.br.service;

import com.banco.pagamento.br.service.dto.PessoaDTO;
import java.util.Optional;

/**
 * Service Interface for managing {@link com.banco.pagamento.br.domain.Pessoa}.
 */
public interface PessoaService {
    /**
     * Save a pessoa.
     *
     * @param pessoaDTO the entity to save.
     * @return the persisted entity.
     */
    PessoaDTO save(PessoaDTO pessoaDTO);

    /**
     * Updates a pessoa.
     *
     * @param pessoaDTO the entity to update.
     * @return the persisted entity.
     */
    PessoaDTO update(PessoaDTO pessoaDTO);

    /**
     * Partially updates a pessoa.
     *
     * @param pessoaDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<PessoaDTO> partialUpdate(PessoaDTO pessoaDTO);

    /**
     * Get the "id" pessoa.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<PessoaDTO> findOne(Long id);

    /**
     * Delete the "id" pessoa.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
