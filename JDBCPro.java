/*
改善メモ:
primary keyなどに非対応
関係Dbからまとめて入力
*/
import java.sql.*;
import java.util.Scanner;

public class JDBCPro {
    public static void main(String[] args) throws Exception {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String table_name = null;//テーブル名
        String sql_statement = null;//実行sql文
        int collum_count = 0;//属性列をカウント
        String field = null;//属性名。配列fieldsに格納
        String type = null;//型。配列typesに格納。
        String[] types = new String[50];
        String[] fields = new String[50];
        boolean[] modes = new boolean[50];//モードを記録しておく配列。trueで自動入力モード。
        String[] sub_str = new String[50];//自動入力モードにおける切り出し文字を格納。
        int mode = 1;//mode1は通常入力,mode2は自動入力を表す
        int record_count = 1;//data[0][i]=nullなので、record_count-1が実際のデータの個数である。
        String key = null;//入力値を格納。
        String[][] data = new String[100][50];//入力されたデータを格納。行でレコード、列でカラムを管理。システムの仕様上、data[0][i]はnullとなる。
        Scanner scan = new Scanner(System.in);
        boolean flug = false;//trueでループ文をi = 1から開始

        

        try{
            //Class.forName("com.mysql.jdbc.Driver");
            //使用するデータベースを選択
            System.out.print("使用するDB:");
            String database_name = scan.nextLine();
            con = DriverManager.getConnection("jdbc:mysql://localhost/" + database_name + "?user=root&password=パスワード");
            System.out.println(database_name + "にアクセスしました。\n");

            //作られているテーブル名を表示。
            System.out.println(":::::" + database_name + "内のテーブル:::::\n");
            sql_statement = "show tables;";
            ps = con.prepareStatement(sql_statement);
            rs = ps.executeQuery();
            while(rs.next()){
                System.out.println(rs.getString("Tables_in_" + database_name));
            }
            ps.close();
            rs.close();

            //使用するsql文を選択
            while(true){
                //配列の中身を初期化
                for(int p = 0; p < 50; p++){
                    types[p] = null;
                    fields[p] = null;
                    modes[p] = false;
                    sub_str[p] = null;
                    for(int q = 0; q < 100; q++){
                        data[q][p] = null;
                    }
                }
                collum_count = 0;
                flug = false;
                System.out.print("\n1.create\n2.insert\n3.drop table\n\n1~3で選択してください。:");
                int menu = scan.nextInt();
                scan.nextLine();
                //menu1 ↓tableを作成
                if(menu==1){
                    System.out.print("\ncreate文を作成します。\n\nテーブル名:");
                    table_name = scan.nextLine();

                    //データ入力し格納
                    int count = 0;
                    while(true){
                        System.out.println("\n終了キー:f キャンセル:x\nvarchar(num):vnum, int(num):inum, date:d");
                        System.out.print("属性名:");
                        key = scan.nextLine();
                        if(key.equals("f")){
                            break;
                        }else if(key.equals("x")){
                            count--;
                            System.out.println("キャンセルされました。\n" + fields[count]);
                        }else{
                            fields[count] = key;
                        }
                        System.out.print("型：");
                        key = scan.nextLine();
                        if(key.equals("f")){
                            break;
                        }else if(key.equals("x")){
                            System.out.println("キャンセルされました。");
                            continue;
                        }else if(key.substring(0, 1).equals("i")){
                            types[count] = "int(" + key.substring(1, key.length()) + ")";
                            count++;
                        }else if(key.equals("d")){
                            types[count] = "date";
                            count++;
                        }else if(key.substring(0, 1).equals("v")){
                            types[count] = "varchar(" + key.substring(1, key.length()) + ")";
                            count++;
                        }else{
                            System.out.println("erorr:入力し直してください。");
                        }
                    }
                    //デバッグ(配列の中身)
                    /*for(int x = 0; x < fields.length; x++){
                        System.out.println(types[x] + " ");
                    }

                    for(int y = 0; y < types.length; y++){
                        System.out.println(fields[y] + " ");
                    }*/

                    //create文の生成
                    sql_statement = "create table " + table_name + "(" + fields[0] + " " + types[0];
                    for(int c = 1; c < count; c++){
                        sql_statement = sql_statement + ", " + fields[c] + " " + types[c];
                    }
                    sql_statement = sql_statement + ")";
                    System.out.println(sql_statement);
                    ps = con.prepareStatement(sql_statement);
                    ps.executeUpdate();//ここで作成
                    System.out.println(table_name + "を作成しました。");
                    System.out.println(":::::" + database_name + "内のテーブル:::::\n");
                    sql_statement = "show tables;";
                    ps.close();
                    ps = con.prepareStatement(sql_statement);
                    rs = ps.executeQuery();
                    while(rs.next()){
                        System.out.println(rs.getString("Tables_in_" + database_name));
                    }
                    System.out.print("\ny:はい n:いいえ\n続けてデータ入力しますか？:");
                    key = scan.nextLine();
                    if(key.equals("y")){
                        continue;
                    }else{
                        break;
                    }
                //menu2 ↓insertでデータ入力。
                }else if(menu==2){
                    record_count = 1;
                    System.out.print("\ninsert文を作成します。\n\nテーブル名:");
                    //ここでテーブルの属性を確認
                    table_name = scan.nextLine();
                    sql_statement = "desc " + table_name;
                    ps = con.prepareStatement(sql_statement);
                    rs = ps.executeQuery();
                    System.out.println("属性は以下の通りです。\n");
                    while(rs.next()){
                        //fieldとtypeを配列に格納
                        field = rs.getString("field");
                        type = rs.getString("type");
                        System.out.println(field + " " + type + "\t");
                        fields[collum_count] = field;
                        types[collum_count] = type;
                        collum_count++;
                    }
                    //データ入力し格納
                    sql_statement = "insert into " + table_name + " values(";
                    for(int x = 1; x < collum_count; x++){
                        sql_statement = sql_statement + "?, ";
                    }
                    sql_statement = sql_statement + "?)";
                    ps.close();
                    ps = con.prepareStatement(sql_statement);
                    while(true){
                        //1レコード記録
                        for(int i = 0; i < collum_count; i++){
                            if(flug==true){
                                i = 1;
                                flug = false;
                            }
                            switch(mode){
                                case 1://普通入力モード
                                if(modes[i]==true){
                                    mode = 2;
                                    i--;
                                    break;
                                }
                                System.out.println("\n入力されたレコード:" + (record_count-1));
                                System.out.println(":::::普通入力モード:::::");
                                System.out.println("終了:f コピー:Enter キャンセル:x 自動入力モード:j\n");
                                System.out.print(fields[i] + " " + types[i] + ":");
                                key = scan.nextLine();
                                if(key.equals("f")){
                                    break;
                                }else if(key.isEmpty()){
                                    data[record_count][i] = data[record_count-1][i];
                                    if(checkType(types[i])==true){
                                        ps.setInt(i+1, Integer.parseInt(data[record_count][i]));
                                    } else {
                                        ps.setString(i+1, data[record_count][i]);
                                    }
                                    break;
                                }else if(key.equals("x")){
                                    if(i==0){
                                        System.out.println("キャンセルされました。");
                                        break;
                                    } else if(i==1){
                                        flug = true;
                                        System.out.println("キャンセルされました。");
                                        break;
                                    }
                                    i = i -2;
                                    System.out.println("キャンセルされました。");
                                    continue;
                                }else if(key.equals("j")){
                                    modes[i] = true;
                                    System.out.println("自動入力する文字列を入力してください:");
                                    key = scan.nextLine();
                                    sub_str[i] = key;
                                    mode = 2;
                                    i--;
                                    break;
                                } else {
                                    data[record_count][i] = key;
                                    if(checkType(types[i])==true){
                                        ps.setInt(i+1, Integer.parseInt(key));
                                    } else {
                                        ps.setString(i+1, key);
                                    }
                                }
                                break;

                                case 2://自動入力モード
                                if(modes[i]==false){
                                    mode = 1;
                                    i--;
                                    break;
                                }
                                System.out.println("\n入力されたレコード:" + (record_count-1));
                                System.out.println(":::::自動入力モード:::::");
                                System.out.println("終了:fキー コピー:Enter キャンセル:x 自動入力終了:j\n");
                                System.out.print(fields[i] + " " + types[i] + ":" + sub_str[i]);
                                key = scan.nextLine();
                                if(key.equals("f")){
                                    break;
                                }else if(key.isEmpty()){
                                    data[record_count][i] = data[record_count-1][i];
                                    if(checkType(types[i])==true){
                                        ps.setInt(i+1, Integer.parseInt(data[record_count][i]));
                                    } else {
                                        ps.setString(i+1, data[record_count][i]);
                                    }
                                    break;
                                }else if(key.equals("x")){
                                    if(i==0){
                                        System.out.println("キャンセルされました。");
                                        break;
                                    } else if(i==1){
                                        flug = true;
                                        System.out.println("キャンセルされました。");
                                        break;
                                    }
                                    i = i -2;
                                    System.out.println("キャンセルされました。");
                                    continue;
                                }else if(key.equals("j")){
                                    modes[i] = false;
                                    mode = 1;
                                    i--;
                                    break;
                                } else {
                                    data[record_count][i] = sub_str[i] + key;
                                    if(checkType(types[i])==true){
                                        ps.setInt(i+1, Integer.parseInt(data[record_count][i]));
                                    } else {
                                        ps.setString(i+1, data[record_count][i]);
                                    }
                                }
                                break;
                            }
                            if(key.equals("f") || key.equals("x")){
                                break;
                            }
                        }
                        if(key.equals("f")){
                            break;
                        } else if(key.equals("x")){
                            continue;
                        }
                        System.out.println(sql_statement);
                        ps.executeUpdate();
                        record_count++;
                    }
                    //デバッグ(配列の中身)
                    /*
                    for(int x = 0; x < record_count; x++){
                        for(int y = 0; y < collum_count; y++){
                            System.out.println("data[" + x + "]" + "[" + y + "]" + ":" + data[x][y]);
                        }

                    }
                    */
                    System.out.println("\nデータが入力されました。\n入力されたレコード数:" + (record_count-1));
                    System.out.print("\ny:はい n:いいえ\n続けてデータ入力しますか？:");
                    key = scan.nextLine();
                    if(key.equals("y")){
                        continue;
                    }else{
                        break;
                    }
                }else if(menu==3){
                    System.out.println(":::::" + database_name + "内のテーブル:::::\n");
                    sql_statement = "show tables;";
                    ps = con.prepareStatement(sql_statement);
                    rs = ps.executeQuery();
                    while(rs.next()){
                        System.out.println(rs.getString("Tables_in_" + database_name));
                    }
                    System.out.println("テーブル削除を行います。");

                    System.out.print("テーブル名:");
                    table_name = scan.nextLine();
                    sql_statement = "drop table " + table_name;
                    ps.close();
                    rs.close();
                    ps = con.prepareStatement(sql_statement);
                    ps.executeUpdate();
                    System.out.println("テーブルが削除されました。");
                    System.out.println(":::::" + database_name + "内のテーブル:::::\n");
                    sql_statement = "show tables;";
                    ps = con.prepareStatement(sql_statement);
                    rs = ps.executeQuery();
                    while(rs.next()){
                        System.out.println(rs.getString("Tables_in_" + database_name));
                    }
                    System.out.print("\ny:はい n:いいえ\n続けてデータ入力しますか？:");
                    key = scan.nextLine();
                    if(key.equals("y")){
                        continue;
                    }else{
                        break;
                    }
                }else{
                    System.out.println("選び直してください。");
                }  

            }
            
            scan.close();
        }catch(SQLException e){
            e.printStackTrace();
        }finally{
            if(con != null){
                try{
                    con.close();
                }catch(SQLException e){
                    e.printStackTrace();
                }
            }
            if(ps != null){
                try{
                    ps.close();
                }catch(SQLException e){
                    e.printStackTrace();
                }
            }
            if(rs != null){
                try{
                    rs.close();
                }catch(SQLException e){
                    e.printStackTrace();
                }
            }
        }
    }
    //intならtrue、文字型ならfalse
    public static boolean checkType(String type){
        if(type.substring(0, 2).equals("int")){
            return true;
        }else{
            return false;
        }
    }
}