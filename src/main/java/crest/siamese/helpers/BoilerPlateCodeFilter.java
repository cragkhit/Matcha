package crest.siamese.helpers;


import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class BoilerPlateCodeFilter {
    private String boilerPlateCodePatternFile;
    public BoilerPlateCodeFilter(String file){
        this.boilerPlateCodePatternFile = file;
    }
    public boolean Filter(String methodName){
        try {

            Path path = Paths.get(boilerPlateCodePatternFile);
            List<String> boilerplateCodePatternList = Files.readAllLines(path, StandardCharsets.UTF_8);

            for (String boilerplateCodePattern : boilerplateCodePatternList){
                Pattern pattern = Pattern.compile(boilerplateCodePattern, Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(methodName);
                if (matcher.find()){
                    return true;
                }
            }
            return false;
        }
        catch (IOException e){
            throw new RuntimeException(e);
        }
    }
    }
