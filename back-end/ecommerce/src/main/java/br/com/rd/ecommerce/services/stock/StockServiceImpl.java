package br.com.rd.ecommerce.services.stock;

import br.com.rd.ecommerce.converters.Converter;
import br.com.rd.ecommerce.models.dto.ProductDTO;
import br.com.rd.ecommerce.models.dto.StockDTO;
import br.com.rd.ecommerce.models.dto.StockProductDTO;
import br.com.rd.ecommerce.models.entities.OrderItem;
import br.com.rd.ecommerce.models.entities.Product;
import br.com.rd.ecommerce.models.entities.Stock;
import br.com.rd.ecommerce.models.entities.StockProduct;
import br.com.rd.ecommerce.repositories.StockRepository;
import br.com.rd.ecommerce.services.exceptions.StockException;
import org.hibernate.JDBCException;
import org.hibernate.exception.SQLGrammarException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class StockServiceImpl implements StockService {

    @Autowired
    private StockRepository repository;
    @PersistenceContext
    private EntityManager em;
    private Converter converter = new Converter();

    @Override
    public ResponseEntity<?> findItemOnStock(Long stock, Long product) {
        Query query = em.createQuery("select p from StockProduct p inner join Stock s on s.id = p.stock where p.product = " + product + " and s.id = "
                + stock, StockProduct.class);

        List<StockProduct> stockProducts = query.getResultList();
        if (stockProducts == null || stockProducts.size() <= 0)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        StockProduct sp = stockProducts.stream().findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));
        StockProductDTO spDTO = converter.convertTo(sp);

        return ResponseEntity.ok().body(spDTO);
    }

    @Override
    public ResponseEntity<?> findAllStocks() {

        List<Stock> stocks = repository.findAll();
        if (stocks.size() <= 0)
            return ResponseEntity.notFound().build();
        List<StockDTO> stocksDTO = new ArrayList<>();
        for (Stock s : stocks)
            stocksDTO.add(converter.convertTo(s));

        return ResponseEntity.ok().body(stocksDTO);

    }

    @Override
    public ResponseEntity<?> findItemInAllStocks(Long product) {
        Query query = em.createQuery("select p from StockProduct p inner join Stock s on s.id = p.stock where p.product =" + product, StockProduct.class);

        List<StockProduct> stockProducts = query.getResultList();
        if (stockProducts == null || stockProducts.size() <= 0)
            return ResponseEntity.badRequest().body(new StockException("Nenhum produto encontrado"));

        List<StockProductDTO> spDTO = new ArrayList<>();
        for (StockProduct sp : stockProducts)
            spDTO.add(converter.convertTo(sp));

        return ResponseEntity.ok().body(spDTO);
    }


    @Override
    public ResponseEntity<?> addItemOnStock(Long idStock, Long idproduct, Integer quantity) {
        Stock stock = repository.findById(idStock).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Stock not found"));

        StockProduct sp = stock.getStockProducts().stream().filter(x ->
                x.getProduct().getId().equals(idproduct)).findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        if (quantity <= 0)
            return ResponseEntity.badRequest().body(new StockException("Invalid quantity"));
        sp.setBalance(sp.getBalance() + quantity);
        StockDTO returnStock = converter.convertTo(repository.save(stock));
        return ResponseEntity.ok().body(returnStock);

    }

    @Override
    public ResponseEntity<?> updateItemOnStockByOrder(Long stock, OrderItem productDTO) throws StockException {
        if (productDTO.getQuantity() <= 0)
            return ResponseEntity.badRequest().body(new StockException("Invalid quanaity"));

        Stock s = repository.findById(stock).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Stock not found"));

        StockProduct sp = s.getStockProducts().stream().filter(x ->
                x.getProduct().getId().equals(productDTO.getProduct().getId())).findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        if (sp.getBalance() < productDTO.getQuantity())
            throw new StockException("No stock for this quantity");
        sp.setBalance(sp.getBalance() - productDTO.getQuantity());
        StockDTO returnStock = converter.convertTo(repository.save(s));
        return ResponseEntity.ok().body(returnStock);
    }

    @Override
    public ResponseEntity<?> registerProductOnStock(Long stock, ProductDTO productDTO) {
        if (productDTO == null)
            return ResponseEntity.badRequest().body(new StockException("Product is not be null"));
        Stock s = repository.findById(stock).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Stock not found"));

        StockProduct find = s.getStockProducts().stream().filter(x -> x.getProduct().getId().equals(productDTO.getId())).findFirst().orElse(null);
        if (find != null)
            return ResponseEntity.badRequest().body(new StockException("O produto já existe no estoque"));
        StockProduct sp = new StockProduct();
        sp.setProduct(converter.convertTo(productDTO));
        sp.setBalance(0);
        s.addProduct(sp);
        StockDTO stockDTO = converter.convertTo(repository.save(s));
        return ResponseEntity.ok().body(stockDTO);

    }

    @Override
    public ResponseEntity<?> getNotRegisteredItems() {
        Query query = em.createQuery("select p from Product p left join StockProduct sp on p.id = sp.product where sp.stock is null", Product.class);
        Set<Product> returnProducts = new HashSet<>();

        List<Product> products = query.getResultList();
        if (products == null != products.size() <= 0)
            return ResponseEntity.badRequest().body(new StockException("Nenhum item encontrado"));
        for (Product p : products)
            returnProducts.add(p);
        return ResponseEntity.ok().body(returnProducts);

    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> handlerEntityExceptionException(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex);
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<?> handlerSQLException(SQLException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex);
    }

    @ExceptionHandler(JDBCException.class)
    public ResponseEntity<?> handlerJDBCException(JDBCException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex);
    }

    @ExceptionHandler(SQLGrammarException.class)
    public ResponseEntity<?> handlerSQLGrammarException(SQLGrammarException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex);
    }

}
