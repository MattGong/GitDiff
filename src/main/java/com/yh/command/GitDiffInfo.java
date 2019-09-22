package com.yh.command;


import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.io.File;

@Getter
@Builder
@ToString
public class GitDiffInfo {
    @NonNull()
    private String baseRef;
    @NonNull
    private String newRef;
    @NonNull
    private String gitUrl;
    @NonNull
    private String name;
    @NonNull
    private String password;
}
