import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
INSERT VALUES ‘lastName’ = ‘Федоров’ , ‘id’=3, ‘age’=40, ‘active’=true
 */


public class DataBaseJava {
     private List<Map<String, Object>> collection;

    public DataBaseJava() {
        collection = new ArrayList<>();
    }

    public List<Map<String, Object>> executeCommand(String request){
        String[] requestSplit = request.split(" ");
        String param = requestSplit[0].toLowerCase();
        switch (param){
            case "insert":
                return insert(request);
            case "update":
                return update(request);
            case "delete":
                return delete(request);
            case "select":
                return select(request);
            default: throw new IllegalArgumentException("Неверный запрос");
        }
    }

    private List<Map<String, Object>> insert(String insert) {
        String parseInsert = parserValues(insert);
        Map<String, Object> map = new HashMap<>();
        String[] insertValueSplit = parseInsert.split(" ");
        for (int i = 2;i < insertValueSplit.length-1;i +=  2){
            map.put(insertValueSplit[i], insertValueSplit[i+1]);
        }
        collection.add(map);
        return collection;
    }

    private List<Map<String, Object>> update(String update) {
        String parseUpdate = update.toLowerCase();
        Map<String, Object> setValues = new HashMap<>();
        String[] updateValueSplit = parseUpdate.split("where");//разделяем строку на две части - значения/условия
        String parseUpdateOne = updateValueSplit[0]; //строка со значениями
        String parseUpdateTwo = updateValueSplit[1]; //строка с условиями
        String splitParseUpdateOne = parserValues(parseUpdateOne);//убираем из строки parseUpdateOne все знаки, и оставляем только пробелы иежду значениями
        String[] arraySplitParseUpdateOne = splitParseUpdateOne.split(" "); //создаём массив из значений и ключей
        for (int i = 2;i < arraySplitParseUpdateOne.length-1;i+=2){
            setValues.put(arraySplitParseUpdateOne[i], arraySplitParseUpdateOne[i+1]); //забили мапу// значениями которые нужно добавить
        }

        Map<String, Object> whereValues = new HashMap<>(); //мапа со значениями, которые стоят после оператора WHERE
        String splitParseUpdateTwo = parserValues(parseUpdateTwo);//убираем из строки parseUpdateTwo все знаки, и оставляем только пробелы между значениями
        String[] arraySplitParseUpdateTwo = splitParseUpdateTwo.split(" "); //создаём массив из значений и ключей чтобы проверить длину для условия цикла

        if(arraySplitParseUpdateTwo.length==4){//тут исправлял, было length>2 !!!
            String[] arraySplitParseUpdateTwoAnd = splitParseUpdateTwo.split("and");
            String[] arrayBeforeAnd = arraySplitParseUpdateTwoAnd[0].split(" "); //создаём массив с ключом и значением, которые стоят перед оператором AND
            String[] arrayAfterAnd = arraySplitParseUpdateTwoAnd[1].split(" "); //создаём массив с ключом и значением, которые после перед оператора AND
            whereValues.put(arrayBeforeAnd[0], arrayBeforeAnd[1]); // добавляем в мапу ключ/значение из масива arrayBeforeAnd, то есть со значения ми стоящими до оператора AND
            whereValues.put(arrayAfterAnd[0], arrayAfterAnd[1]); // добавляем в мапу ключ/значение из масива arrayAfterAnd, то есть со значения ми стоящими после оператора AND
        }

        else if(arraySplitParseUpdateTwo.length==2){
            String[] arrayValues = splitParseUpdateTwo.split(" ");
            whereValues.put(arrayValues[0], arrayValues[1]);
        }

        /*на данном этапе мы получили две мапы setValues и whereValues. В мапе setValues у нас находятся значения
        которыми нужно заменить уже существующие. А в мапе whereValues находятся значения которые стоят после оператора where
        и она задают условия для добавление значений из мапы setValues
        */

        for(Map<String, Object> row : collection){ //в этом цикле мы проверяем есть ли в мапе такие же ключи
            //если нет, то выходим из цикла, если есть то в следующем
            //внутреннем цикле меняем старые значения на новые
            boolean counter = true;
            for(Map.Entry<String, Object> whereEntry : whereValues.entrySet()) {
                String keyWhere = whereEntry.getKey();
                Object valueWhere = whereEntry.getValue();
                if (!row.containsKey(keyWhere) || !row.get(keyWhere).equals(valueWhere)) {
                    counter = false;
                    break;
                }
            }
            if (counter){
                for(Map.Entry<String, Object> setEntry : setValues.entrySet()){
                    String keySet = setEntry.getKey();
                    Object valueSet = setEntry.getValue();
                    row.put(keySet, valueSet);
                }
            }
        }
        return collection;
    }

    private List<Map<String, Object>> delete(String delete) {
        String deleteLowerCase = delete.toLowerCase();
        String[] split = deleteLowerCase.split("where");
        String splitDeleteStr = split[1];//в переменной splitDeleteStr хранится строка после оператора where
        String lengthSplitDelete = parserValues(splitDeleteStr); //эта строка содержит в себе строку значений
        // после оператора where, без знаков препинания и т. п.
        String[] arraySplitLength = lengthSplitDelete.split(" "); //этот массив нужен для проверки длины

        Map<String, Object> whereValues = new HashMap<>();

        if(arraySplitLength.length==2){
            whereValues.put(arraySplitLength[0], arraySplitLength[1]);
        }
        else if(arraySplitLength.length>2){
            String splitNotAnd = lengthSplitDelete.replaceAll("\\band\\b", ""); //убираем из строки все троки "and"
            String[] arraySplitLengthAnd = splitNotAnd.split(" "); //сплитим строки
            for (int i = 0; i < arraySplitLengthAnd.length/2; i++) {
                whereValues.put(arraySplitLengthAnd[i], arraySplitLengthAnd[i+1]);
            }
        }

        // здесь мы имеем мапу заполненную значениями
        List<Map<String, Object>> newCollection = new ArrayList<>();//создаём новую коллекцию для добавления значений
        for(Map<String, Object> row : collection){ //в этом цикле мы проходимся по строкам, и проверяем удовлетворяет ли строка
            //условию, если удовлетворяет, то мы не добавляем её в новый список newCollection
            boolean counter = true;
            for(Map.Entry<String, Object> whereEntry : whereValues.entrySet()){
                String key = whereEntry.getKey();
                Object value = whereEntry.getValue();
                if(!row.containsKey(key) || !row.get(value).equals(value)){
                    counter = false;
                    break;
                }
            }
            if(!counter){
                newCollection.add(row); //теперь мы в новый список добавляем только те значения которые не прошли по условию
                //тоесть грубо говоря мы удалили строку с нашими значениями
            }
            collection = newCollection;
        }
        return collection;
    }

    private List<Map<String, Object>> select(String select){
        return collection;
    }

    public String parserValues(String str) {
        // Заменяем все знаки препинания и обособления на пробелы
        str = str.replaceAll("[^\\p{L}\\p{N}\\s]", " ");
        // Удаляем все лишние пробелы из строки
        str = str.replaceAll("\\s+", " ");
        // Возвращаем полученную строку
        return str;
    }
    }
