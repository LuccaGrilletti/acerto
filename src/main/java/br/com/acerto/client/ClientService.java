package br.com.acerto.client;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClientService {

    private static final String PHONE_CONSTRAINT = "uq_clients_phone";

    private final ClientRepository repository;
    @Transactional
    public Client register(String name, String phone) {
        return saveOrThrow(Client.create(name, phone));
    }

    @Transactional
    public Client update(UUID id, String name, String phone) {
        Client client = get(id);
        client.update(name, phone);
        return saveOrThrow(client);
    }

    @Transactional
    public void deactivate(UUID id) {
        get(id).deactivate();
    }

    @Transactional
    public void activate(UUID id) {
        get(id).activate();
    }

    @Transactional(readOnly = true)
    public List<Client> list() {
        return repository.findAll(Sort.by("name"));
    }

    @Transactional(readOnly = true)
    public Client get(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ClientNotFoundException(id));
    }

    private Client saveOrThrow(Client client) {
        try {
            return repository.saveAndFlush(client);
        } catch (DataIntegrityViolationException e) {
            if (e.getCause() instanceof ConstraintViolationException cve
                    && PHONE_CONSTRAINT.equals(cve.getConstraintName())) {
                throw new DuplicatePhoneException(client.getPhone());
            }
            throw e;
        }
    }
}
