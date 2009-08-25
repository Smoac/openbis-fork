import java.util.Collection;
import java.util.Set;
import java.util.TreeMap;

/*
 * Copyright 2009 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * 
 *
 * @author walshs
 */
public class SampleWithProperties {

    String sname = new String(); // sample name
    TreeMap<String,String> pvs = new TreeMap<String,String>();
    
    public SampleWithProperties (String s)
    {
            sname = s;
    }
    
    public void addProperty(String k, String v){
        pvs.put(k, v);
    }
    
    public String getName(){
        return sname;
    }
    
    public Set<String> getPropertyTypes(){
         return pvs.keySet();
        
    }
    
    public Collection<String> getPropertyValues(){
        return pvs.values();  
   }
   
    
    
}
