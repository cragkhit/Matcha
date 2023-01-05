package crest.siamese.helpers;

import crest.siamese.document.Document;

import javax.print.Doc;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LatestResultVersionRetriever {
    private ArrayList<Document> esResults;
    public LatestResultVersionRetriever(ArrayList<Document> results){
        this.esResults = results;
    }

    public ArrayList<Document> RetrieveLatestResult(){
        ArrayList<Document> result = new ArrayList<Document>();
        String mostSimilarFile = esResults.get(0).getFile().trim();
        String[] filePaths = mostSimilarFile.split("/");
        String filePath = filePaths[filePaths.length-1];
        String fileName = filePath.split("\\.")[0];
        String[] fileNames = fileName.split("_");
        String fileNameWithOutVersion = String.join("_", RemoveArrayByIndex(fileNames,fileNames.length-1));
        String fileVersion = fileNames[fileNames.length-1];
        if(fileVersion.equals("latest")){
            result.add(esResults.get(0));
            return result;
        }
        String newestVersion = fileVersion;
        for (Document esResult : esResults) {
            String[] paths = esResult.getFile().split("/");
            String path = paths[paths.length-1];
            String name = path.split("\\.")[0];
            String[] names = name.split("_");
            String nameWithOutVersion = String.join("_", RemoveArrayByIndex(names,names.length-1));
            String version = names[names.length-1];
            if(fileNameWithOutVersion.equals(nameWithOutVersion)){
                if(version.equals("latest")){
                    result = new ArrayList<>();
                    result.add(esResult);
                    return result;
                }else if(Integer.parseInt(version) >= Integer.parseInt(newestVersion)){
                    result = new ArrayList<>();
                    result.add(esResult);
                    newestVersion = version;
                }
            }
        }
        return result;
    }

    private String[] RemoveArrayByIndex(String[] array, int index)
    {
        List<String> list = new ArrayList<>(Arrays.asList(array));
        list.remove(array[index]);
        return list.toArray(new String[0]);
    }
}
