package com.banco.pagamento.br.service.impl;

import com.banco.pagamento.br.domain.Pessoa;
import com.banco.pagamento.br.repository.PessoaRepository;
import com.banco.pagamento.br.service.PessoaService;
import com.banco.pagamento.br.service.dto.PessoaDTO;
import com.banco.pagamento.br.service.mapper.PessoaMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.banco.pagamento.br.domain.Pessoa}.
 */
@Service
@Transactional
public class PessoaServiceImpl implements PessoaService {

    private static final Logger LOG = LoggerFactory.getLogger(PessoaServiceImpl.class);

    private final PessoaRepository pessoaRepository;

    private final PessoaMapper pessoaMapper;

    public PessoaServiceImpl(PessoaRepository pessoaRepository, PessoaMapper pessoaMapper) {
        this.pessoaRepository = pessoaRepository;
        this.pessoaMapper = pessoaMapper;
    }

    @Override
    public PessoaDTO save(PessoaDTO pessoaDTO) {
        LOG.debug("Request to save Pessoa : {}", pessoaDTO);
        Pessoa pessoa = pessoaMapper.toEntity(pessoaDTO);
        pessoa = pessoaRepository.save(pessoa);
        return pessoaMapper.toDto(pessoa);
    }

    @Override
    public PessoaDTO update(PessoaDTO pessoaDTO) {
        LOG.debug("Request to update Pessoa : {}", pessoaDTO);
        Pessoa pessoa = pessoaMapper.toEntity(pessoaDTO);
        pessoa.setIsPersisted();
        pessoa = pessoaRepository.save(pessoa);
        return pessoaMapper.toDto(pessoa);
    }

    @Override
    public Optional<PessoaDTO> partialUpdate(PessoaDTO pessoaDTO) {
        LOG.debug("Request to partially update Pessoa : {}", pessoaDTO);

        return pessoaRepository
            .findById(pessoaDTO.getId())
            .map(existingPessoa -> {
                pessoaMapper.partialUpdate(existingPessoa, pessoaDTO);

                return existingPessoa;
            })
            .map(pessoaRepository::save)
            .map(pessoaMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PessoaDTO> findOne(Long id) {
        LOG.debug("Request to get Pessoa : {}", id);
        return pessoaRepository.findById(id).map(pessoaMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Pessoa : {}", id);
        pessoaRepository.deleteById(id);
    }
}
