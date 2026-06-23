Допустим, у нас есть класс с таким методом 

```java
Coroutine<String> example() {
    System.out.println("first");
    suspend();
    for (int i = 0; i < 10; i += 1) {
        System.out.println("cycle " + i);
        suspend();
    }
    
    Coroutine<String> otherCoroutine = getOtherStringCoroutine();
    
    while (!otherCoroutine.finished()) {
        System.out.println("waiting for other string...");
        suspend();
        otherCoroutine.continueExecution();
    }
    var otherString = otherCoroutine.getResult();
    
    System.out.println("Other string is: " + otherString);
    return result("Success!");
}
```

Этот метод надо превратить в два метода. Один должен создавать объект-корутину, а второй должен выполнять корутину.
```java
Coroutine<String> example(params...) {
    var continuation = new Continuation();
    continuation.store(params...);
    return new CoroutineImpl<Stirng>(continuation, this::example$coroutine);
}

String example$coroutine(Continuation continuation) {
    int contLabel = continuation.label;
    // iload contLabel;
    // ifeq (адрес лейбла start)
    // iinc contLabel -1
    // iload contLabel;
    // ifeq (адрес лейбла label_1)
    // iinc contLabel -1
    // iload contLabel;
    // ifeq (адрес лейбла label_2)
    // iinc contLabel -1
    // iload contLabel;
    // ifeq (адрес лейбла label_3)
    throw new IllegalArgumentException("Continuation has illegal label value " + continuation.label); 
    start:
    System.out.println("first");
    continuation.label = label_1;                  // -\
    // *_* внести в continuation текущий фрейм +_+ // --\
    return null;                                   // ---> слово suspend()
    label_1:                                       // --/
    // +_+ достать фрейм из continuation *_*       // -/
    for (int i = 0; i < 10; i += 1) {
        System.out.println("cycle " + i);
        continuation.label = label_2;                  // -\
        // *_* внести в continuation текущий фрейм +_+ // --\
        return null;                                   // ---> слово suspend() 
        label_2:                                       // --/
        // +_+ достать фрейм из continuation *_*       // -/
    }
    Coroutine<String> otherCoroutine = getOtherStringCoroutine();

    while (!otherCoroutine.finished()) {
        System.out.println("waiting for other string...");
        // *_* внести в continuation текущий фрейм +_+ // -\
        continuation.label = label_3;                  // --\
        return null;                                   // ---> слово suspend()
        label_3:                                       // --/
        // +_+ достать фрейм из continuation *_*       // -/
        otherCoroutine.continueExecution();
    }
    var otherString = otherCoroutine.getResult();

    System.out.println("Other string is: " + otherString);
    
    continuation.finished = true;
    return "Success!";
}
```

Подзадачи получается такие:
1. как-то реализовать goto continuation.label [V]
2. заменять все suspend() на следующие фрагменты инструкций: [V]
   1. continuation.label = текущий лейбл 
   2. копирование текущего фрейма в объект continuation 
      > просто набор инструкций вида continuation.store(...). Сначала для переменных, потом - для стека
   3. return null 
   4. объявление текущего лейбла
   5. восстановление фрейма из объекта continuation
      > просто набор инструкций вида continuationn.load<Type>()
3. заменять все `result(result)` на `continuation.finished = true` и `return result;` [V]
4. понять, сколько переменных в текущем фрейме и какая глубина стека? 


выдержки из спеки
- If the class file version number is 51.0 or above, then instances of instructions using the jsr, jsr_w, or ret opcodes must not appear in the code array.




