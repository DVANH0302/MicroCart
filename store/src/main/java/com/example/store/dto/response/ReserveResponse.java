package com.example.store.dto.response;

import java.util.*;

import lombok.Setter;
import lombok.Getter;
import com.example.store.dto.request.ReleaseRequest;

@Getter @Setter
public class ReserveResponse {
    private Integer orderId;
    private Integer productId;
    private List<ReleaseRequest.Alloc> allocations;    
}
