package com.commitcard.app.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubUserDTO {

    private String login;
    @JsonProperty("name")
    private String nome;
    private String bio;

    @JsonProperty("avatar_url")
    private String avatarUrl;
}
