package com.banco.pagamento.br.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import org.springframework.data.domain.Persistable;

/**
 * A Pessoa.
 */
@Entity
@Table(name = "pessoa")
@JsonIgnoreProperties(value = { "new" })
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Pessoa extends AbstractAuditingEntity<Long> implements Serializable, Persistable<Long> {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Lob
    @Column(name = "foto")
    private byte[] foto;

    @Column(name = "foto_content_type")
    private String fotoContentType;

    @NotNull
    @Size(min = 3, max = 510)
    @Column(name = "nome", length = 510, nullable = false)
    private String nome;

    @NotNull
    @Column(name = "dt_nascimento", nullable = false)
    private LocalDate dtNascimento;

    @NotNull
    @Pattern(regexp = "([0-9]{3}\\.?[0-9]{3}\\.?[0-9]{3}\\-?([0-9]){2})")
    @Column(name = "cpf", nullable = false, unique = true)
    private String cpf;

    @NotNull
    @Pattern(regexp = "(^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$)")
    @Column(name = "email", nullable = false)
    private String email;

    // Inherited createdBy definition
    // Inherited createdDate definition
    // Inherited lastModifiedBy definition
    // Inherited lastModifiedDate definition
    @org.springframework.data.annotation.Transient
    @Transient
    private boolean isPersisted;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Pessoa id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public byte[] getFoto() {
        return this.foto;
    }

    public Pessoa foto(byte[] foto) {
        this.setFoto(foto);
        return this;
    }

    public void setFoto(byte[] foto) {
        this.foto = foto;
    }

    public String getFotoContentType() {
        return this.fotoContentType;
    }

    public Pessoa fotoContentType(String fotoContentType) {
        this.fotoContentType = fotoContentType;
        return this;
    }

    public void setFotoContentType(String fotoContentType) {
        this.fotoContentType = fotoContentType;
    }

    public String getNome() {
        return this.nome;
    }

    public Pessoa nome(String nome) {
        this.setNome(nome);
        return this;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public LocalDate getDtNascimento() {
        return this.dtNascimento;
    }

    public Pessoa dtNascimento(LocalDate dtNascimento) {
        this.setDtNascimento(dtNascimento);
        return this;
    }

    public void setDtNascimento(LocalDate dtNascimento) {
        this.dtNascimento = dtNascimento;
    }

    public String getCpf() {
        return this.cpf;
    }

    public Pessoa cpf(String cpf) {
        this.setCpf(cpf);
        return this;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getEmail() {
        return this.email;
    }

    public Pessoa email(String email) {
        this.setEmail(email);
        return this;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Inherited createdBy methods
    public Pessoa createdBy(String createdBy) {
        this.setCreatedBy(createdBy);
        return this;
    }

    // Inherited createdDate methods
    public Pessoa createdDate(Instant createdDate) {
        this.setCreatedDate(createdDate);
        return this;
    }

    // Inherited lastModifiedBy methods
    public Pessoa lastModifiedBy(String lastModifiedBy) {
        this.setLastModifiedBy(lastModifiedBy);
        return this;
    }

    // Inherited lastModifiedDate methods
    public Pessoa lastModifiedDate(Instant lastModifiedDate) {
        this.setLastModifiedDate(lastModifiedDate);
        return this;
    }

    @PostLoad
    @PostPersist
    public void updateEntityState() {
        this.setIsPersisted();
    }

    @org.springframework.data.annotation.Transient
    @Transient
    @Override
    public boolean isNew() {
        return !this.isPersisted;
    }

    public Pessoa setIsPersisted() {
        this.isPersisted = true;
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Pessoa)) {
            return false;
        }
        return getId() != null && getId().equals(((Pessoa) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Pessoa{" +
            "id=" + getId() +
            ", foto='" + getFoto() + "'" +
            ", fotoContentType='" + getFotoContentType() + "'" +
            ", nome='" + getNome() + "'" +
            ", dtNascimento='" + getDtNascimento() + "'" +
            ", cpf='" + getCpf() + "'" +
            ", email='" + getEmail() + "'" +
            ", createdBy='" + getCreatedBy() + "'" +
            ", createdDate='" + getCreatedDate() + "'" +
            ", lastModifiedBy='" + getLastModifiedBy() + "'" +
            ", lastModifiedDate='" + getLastModifiedDate() + "'" +
            "}";
    }
}
