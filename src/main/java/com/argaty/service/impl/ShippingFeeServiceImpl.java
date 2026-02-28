package com.argaty.service.impl;

import com.argaty.config.AppProperties;
import com.argaty.service.ShippingFeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShippingFeeServiceImpl implements ShippingFeeService {

    private static final List<String> JNT_FEE_FIELDS = List.of(
        "fee", "shippingFee", "freight", "totalFee", "service_fee", "shipFee",
        "totalFreight", "freightAmount", "shippingCost", "cost", "price", "amount"
    );

    private final AppProperties appProperties;

    @Override
    public BigDecimal calculateFee(BigDecimal subtotal,
                                   String city,
                                   String district,
                                   String ward,
                                   String address,
                                   int itemCount) {

        AppProperties.Shipping shipping = appProperties.getShipping();
        AppProperties.Shipping.Jnt jnt = shipping.getJnt();
        String endpoint = resolveEndpoint(jnt);

        if (!jnt.isEnabled() || !StringUtils.hasText(endpoint)) {
            log.info("J&T shipping bị tắt hoặc thiếu endpoint, dùng fallback. enabled={}, endpointPresent={}",
                    jnt.isEnabled(), StringUtils.hasText(endpoint));
            return fallbackFee(subtotal, shipping);
        }

        try {
            RestClient client = buildClient(jnt);
            MultiValueMap<String, String> payload = new LinkedMultiValueMap<>();
            payload.add("apiAccount", safe(jnt.getApiAccount()));
            payload.add("privateKey", safe(jnt.getPrivateKey()));
            payload.add("originCity", safe(jnt.getOriginCity()));
            payload.add("originDistrict", safe(jnt.getOriginDistrict()));
            payload.add("destinationCity", safe(city));
            payload.add("destinationDistrict", safe(district));
            payload.add("destinationWard", safe(ward));
            payload.add("destinationAddress", safe(address));
            payload.add("weightGram", String.valueOf(jnt.getDefaultWeightGram()));
            payload.add("itemCount", String.valueOf(Math.max(itemCount, 1)));
            payload.add("codAmount", String.valueOf(subtotal.max(BigDecimal.ZERO)));
            payload.add("orderAmount", String.valueOf(subtotal.max(BigDecimal.ZERO)));

                payload.add("sendCity", safe(jnt.getOriginCity()));
                payload.add("sendDistrict", safe(jnt.getOriginDistrict()));
                payload.add("destCity", safe(city));
                payload.add("destDistrict", safe(district));
                payload.add("destArea", safe(ward));
                payload.add("destAddress", safe(address));
                payload.add("weight", String.valueOf(jnt.getDefaultWeightGram()));
                payload.add("pieces", String.valueOf(Math.max(itemCount, 1)));
                payload.add("goodsValue", String.valueOf(subtotal.max(BigDecimal.ZERO)));

            RestClient.RequestBodySpec request = client.post()
                    .uri(endpoint)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.APPLICATION_JSON);

            if (StringUtils.hasText(jnt.getApiKey())) {
                request = request.header(jnt.getApiKeyHeader(), jnt.getApiKey());
            }

            if (StringUtils.hasText(jnt.getCustomerCode())) {
                request = request.header("X-Customer-Code", jnt.getCustomerCode());
            }

            String raw = request.body(payload).retrieve().body(String.class);
            Optional<BigDecimal> fee = parseFee(raw);
            if (fee.isPresent()) {
                log.info("J&T shipping fee resolved: {}", fee.get());
                return fee.get().max(BigDecimal.ZERO);
            }

            throw new IllegalStateException("J&T response không có trường phí ship hợp lệ");
        } catch (RuntimeException ex) {
            if (jnt.isFallbackOnError()) {
                log.warn("Không lấy được phí ship J&T, dùng fallback. Reason: {}", ex.getMessage());
                return fallbackFee(subtotal, shipping);
            }
            throw new RuntimeException("Không thể lấy phí ship từ J&T", ex);
        }
    }

    private RestClient buildClient(AppProperties.Shipping.Jnt jnt) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(jnt.getConnectTimeoutMs());
        factory.setReadTimeout(jnt.getReadTimeoutMs());

        return RestClient.builder().requestFactory(factory).build();
    }

    private String resolveEndpoint(AppProperties.Shipping.Jnt jnt) {
        if (jnt.isUseProduction() && StringUtils.hasText(jnt.getProductionRateEndpoint())) {
            return jnt.getProductionRateEndpoint();
        }
        if (!jnt.isUseProduction() && StringUtils.hasText(jnt.getUatRateEndpoint())) {
            return jnt.getUatRateEndpoint();
        }
        return jnt.getRateEndpoint();
    }

    private Optional<BigDecimal> parseFee(String rawJson) {
        if (!StringUtils.hasText(rawJson)) {
            return Optional.empty();
        }

        for (String key : JNT_FEE_FIELDS) {
            Pattern strictKeyPattern = Pattern.compile(
                    "\"" + Pattern.quote(key) + "\"\\s*:\\s*\"?([0-9]+(?:\\.[0-9]+)?)\"?",
                    Pattern.CASE_INSENSITIVE
            );
            Matcher strictKeyMatcher = strictKeyPattern.matcher(rawJson);
            if (strictKeyMatcher.find()) {
                return Optional.of(new BigDecimal(strictKeyMatcher.group(1)));
            }
        }

        Pattern looseKeyPattern = Pattern.compile(
                "\"([^\"]*(fee|freight|cost|price|amount)[^\"]*)\"\\s*:\\s*\"?([0-9]+(?:\\.[0-9]+)?)\"?",
                Pattern.CASE_INSENSITIVE
        );
        Matcher looseKeyMatcher = looseKeyPattern.matcher(rawJson);
        if (looseKeyMatcher.find()) {
            return Optional.of(new BigDecimal(looseKeyMatcher.group(3)));
        }

        log.warn("Không parse được phí ship từ response J&T: {}", rawJson);
        return Optional.empty();
    }

    private BigDecimal fallbackFee(BigDecimal subtotal, AppProperties.Shipping shipping) {
        if (subtotal.compareTo(BigDecimal.valueOf(shipping.getFreeThreshold())) >= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(shipping.getDefaultFee());
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
