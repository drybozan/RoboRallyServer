package com.RoboRallyServer.utilities.results;

public class DataResult<T> extends  Result //<T> dondurulecek veri tipi
    {
       private T data;
      public DataResult(T data, boolean success, String message) {
        super(success, message);
        this.data=data;

    }

        public DataResult(T data, boolean success) {
            super(success); // üst classa gider super ile yani Result'a gider
            this.data=data;

        }
        public  T getData(){
          return this.data;
        }
}
