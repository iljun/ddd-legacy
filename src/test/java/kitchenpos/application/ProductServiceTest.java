package kitchenpos.application;

import kitchenpos.application.fake.FakeMenuRepository;
import kitchenpos.application.fake.FakeProductRepository;
import kitchenpos.application.fake.FakePurgomalumClient;
import kitchenpos.application.support.TestFixture;
import kitchenpos.domain.MenuRepository;
import kitchenpos.domain.Product;
import kitchenpos.domain.ProductRepository;
import kitchenpos.infra.purgomalum.PurgomalumClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    private ProductRepository productRepository;
    private MenuRepository menuRepository;
    private FakePurgomalumClient purgomalumClient;

    private ProductService productService;

    @BeforeEach
    void setup() {
        productRepository = new FakeProductRepository();
        menuRepository = new FakeMenuRepository();
        purgomalumClient = new FakePurgomalumClient();
        productService = new ProductService(productRepository, menuRepository, purgomalumClient);
    }
    @DisplayName("상품 생성이 가능하다")
    @Test
    void create_product() {
        final Product product = TestFixture.createFirstProduct();

        final Product result = productService.create(product);
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(TestFixture.FIRST_PRODUCT_NAME);
        assertThat(result.getPrice()).isEqualTo(TestFixture.FIRST_PRODUCT_PRICE);
    }

    @DisplayName("가격이 비어있거나 음수이면 IllegalArgumentException를 발생시킨다")
    @ParameterizedTest
    @NullSource
    @ValueSource(longs = -1)
    void create_product_by_negative_number(final Long price) {
        Product product = TestFixture.createFirstProduct();
        BigDecimal wrapPrice = Optional.ofNullable(price)
                .map(BigDecimal::new)
                .orElse(null);
        product.setPrice(wrapPrice);

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> productService.create(product));
    }

    @DisplayName("상품의 이름은 필수이다")
    @ParameterizedTest
    @NullAndEmptySource
    void create_product_with_null_and_empty_name(final String name) {
        Product product = TestFixture.createFirstProduct();
        product.setName(name);

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> productService.create(product));
    }

    @DisplayName("상품의 이름은 비속어가 포함될 수 없다")
    @Test
    void create_product_with_profanity() {
        final Product product = TestFixture.createFirstProduct();
        purgomalumClient.changeProfanity(true);

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> productService.create(product));
    }

    @DisplayName("생성된 상품의 가격을 변경할 수 있다")
    @Test
    void change_price() {
        final Product originProduct = TestFixture.createFirstProduct();
        productRepository.save(originProduct);

        final BigDecimal changedPrice = BigDecimal.valueOf(15000L);
        Product updateProduct = TestFixture.createFirstProduct();
        updateProduct.setPrice(changedPrice);

        final Product result = productService.changePrice(TestFixture.FIRST_PRODUCT_ID, updateProduct);

        assertThat(result).isNotNull();
        assertThat(result.getPrice()).isEqualTo(changedPrice);
    }

    @DisplayName("가격을 변경할 때 가격이 Null이거나 음수라면 IllegalArgumentException를 발생시킨다")
    @ParameterizedTest
    @NullSource
    @ValueSource(longs = -1)
    void change_price_by_negative_number(final Long price) {
        Product updateProduct = TestFixture.createFirstProduct();
        BigDecimal wrapPrice = Optional.ofNullable(price)
                .map(BigDecimal::new)
                .orElse(null);
        updateProduct.setPrice(wrapPrice);

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> productService.changePrice(TestFixture.FIRST_PRODUCT_ID, updateProduct));
    }

    @DisplayName("생성된 상품을 조회 가능하다")
    @Test
    void select_all_products() {
        final Product firstProduct = TestFixture.createFirstProduct();
        final Product secondChicken = TestFixture.createSecondProduct();
        productRepository.save(firstProduct);
        productRepository.save(secondChicken);

        final List<Product> chickens = Arrays.asList(firstProduct, secondChicken);

        final List<Product> products = productService.findAll();
        assertThat(products).isNotEmpty();
        assertThat(products.size()).isEqualTo(2);
    }
}
