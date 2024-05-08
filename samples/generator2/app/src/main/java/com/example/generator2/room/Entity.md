
```mermaid
classDiagram
    class Shape
    <<interface>> Shape
    Shape : ...
    Shape : +draw()

    class Circle
    Circle : ...
    Circle : +draw()



    class Round
    Round : ...
    Round : +draw()



    Shape <|.. Circle : Realization
    Shape <|.. Round  : Realization

    style Shape fill:#f9f,stroke:#333,stroke-width:4px
```

