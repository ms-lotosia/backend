package com.lotosia.productservice.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * @author: nijataghayev
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PageableResponse<T> {

    List<T> data;
    long totalElements;
    int lastPageNumber;
    boolean hasNextPage;
}

