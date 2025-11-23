package com.lotosia.productservice.criteria;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.USE_DEFAULTS;

/**
 * @author: nijataghayev
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PageCriteria {

    @JsonInclude(value = USE_DEFAULTS)
    Integer page = 0;
    @JsonInclude(value = USE_DEFAULTS)
    Integer count = 10;
}


