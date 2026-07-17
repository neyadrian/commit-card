package com.commitcard.app.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubRepoDTO {

    @JsonProperty("name")
    private String nome;
    @JsonProperty("description")
    private String descricao;
    @JsonProperty("html_url")
    private String htmlUrl;
    @JsonProperty("stargazers_count")
    private int stargazersCount;
    private int forks;
    private String language;
    private Boolean fork;
}
