package br.com.rd.ecommerce.services.address;

import br.com.rd.ecommerce.models.dto.AddressDTO;
import br.com.rd.ecommerce.models.dto.ClientDTO;
import org.springframework.http.ResponseEntity;

public interface AddressService {
    ResponseEntity<?> findAllAddress();
    ResponseEntity<?> createAddress(AddressDTO addressDTO);
    void deleteAddress(Long id);
    ResponseEntity<?> updateAddress(Long id, AddressDTO addressDTO);
}
