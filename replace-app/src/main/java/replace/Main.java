package replace;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Miguel S
 */

public class Main {

    public static void main(String[] args) {
        boolean usageError = false;

        int delim = 0;
        
        for(String s: args){
            if("--".equals(s)){
                delim++;
            }
        }
        if(args.length > 3){
            switch (delim) {
                case 2:
                    if(!delimiterProcessing(args, 2)){
                        usageError = true;
                    }
                    break;
                case 1:
                    if(!delimiterProcessing(args, 1)){
                        usageError = true;
                    }
                    break; 
                default:
                    usageError = true;
                    break;
            }
        }else{
            usageError = true;
        }
        
        if(usageError)
            usage();

    }
    
    private static boolean delimiterProcessing(String[] args, int delimiter) {
        boolean backUpFile, first, last ,insensitive;
        backUpFile = first = last = insensitive = false;
        String changeFrom = "";
        String changeTo = "";
        ArrayList<String> filePaths = new ArrayList<>();
        boolean flag = true;
        
        for(int i = 0; i < args.length && flag; i++) {
                if("-i".equals(args[i])){
                    insensitive = true;
                } else if ("-l".equals(args[i])){
                    last = true;
                } else if ("-f".equals(args[i])){
                    first = true;
                } else if ("-b".equals(args[i])) {
                    backUpFile = true;
                } else if (!"--".equals(args[i]) && delimiter == 1 && i+3 < args.length) {
                    changeFrom = args[i];
                    ++i;
                    changeTo = args[i];
                    i++;
                    if("--".equals(args[i])){
                        i++;
                        for(;i< args.length;i++){
                           Path path = Paths.get(args[i]);
                            if(Files.exists(path)){
                               filePaths.add(args[i]);
                            }else{
                                flag = false;
                                break;
                            }
                        }
                    }else{
                        flag = false;
                        break;
                    }
                } else if ("--".equals(args[i]) && delimiter == 2 && i+3 < args.length) {
                    ++i;
                    changeFrom = args[i];
                    ++i;
                    changeTo = args[i];
                    ++i;
                    if("--".equals(args[i])){
                        i++;
                        for(;i< args.length;i++){
                           Path path = Paths.get(args[i]);
                            if(Files.exists(path)){
                               filePaths.add(args[i]);
                            }else{
                                flag = false;
                                break;
                            }
                        }
                    }else{
                        flag = false;
                        break;
                    }
                } else {
                    flag = false;
                }
        }
        if(flag){
            processFiles(insensitive, backUpFile, first, last, filePaths, changeFrom, changeTo);
        }
        return flag;
    }
    
    
    private static void processFiles(
                                boolean i, boolean b, boolean f, boolean l,
                                ArrayList<String> filePaths, String from, String to){
        File tempBackUp;
        for(int j = 0; j < filePaths.size(); j++){
            if(b){
                try {
                    tempBackUp = File.createTempFile(filePaths.get(j), ".bck");
                    Files.copy(Paths.get(filePaths.get(j)), Paths.get(tempBackUp.getAbsolutePath()));
                } catch (IOException e) {}
            }
            modifyFile(i, f, l, from, to, filePaths.get(j));
        }
        
    }
    
    private static void modifyFile(boolean i, boolean f, boolean l,String from, String to, String file){
        StringBuilder textFromFile = new StringBuilder();
        BufferedReader reader = null;
        BufferedWriter writer = null;
        String iFrom = "(?i)" + from;
         
        try{
            reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
             
            while (line != null) {
                textFromFile.append(line).append(System.lineSeparator());
                line = reader.readLine();
            }
            
            String modifiedString = textFromFile.toString();  
            if(i){
                if(!f && !l){
                    modifiedString = modifiedString.replaceAll(iFrom, to);
                }else{
                    if(l){
                        modifiedString = modifiedString.replaceAll(iFrom+"$", to);
                    }
                    if(f){
                        modifiedString = modifiedString.replaceFirst(iFrom, to);
                    }
                }

            }else{
                if(!l && !f){
                    modifiedString = modifiedString.replaceAll(from, to);
                }else{
                    if(l){
                        modifiedString = modifiedString.replaceAll(from+"$", to);
                    }
                    if(f){
                        modifiedString = modifiedString.replaceFirst(from, to);
                    }
                }
            }
             
            writer = new BufferedWriter(new FileWriter(file));
            writer.write(modifiedString);
            writer.flush();
        } catch (IOException e){}
        finally{
            try{
                reader.close();
                writer.close();
            } 
            catch (IOException e) {}
        }
    }

    private static void usage() {
        System.err.println("Usage: Replace [-b] [-f] [-l] [-i] <from> <to> -- " + "<filename> [<filename>]*" );
    }

}
