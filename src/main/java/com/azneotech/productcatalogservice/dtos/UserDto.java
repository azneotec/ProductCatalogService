package com.azneotech.productcatalogservice.dtos;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {

    private Long id;
    private String name;
    private String email;
    private String phoneNumber;
    @Builder.Default
    private List<String> roles = new ArrayList<>();

}
