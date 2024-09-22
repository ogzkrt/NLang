### Supported features

#### Defining variables
```html
make a = 10;
make b = 15;
```
#### Assignment
```html
a = 10;
```
#### Evaluate expressions
```terminal
make a = (2*/12)+(4*b);
```
#### Lists

```console
make a = [1,2,3];
print(a);
make b = a.reverse();
print(b);   # [3,2,1]
a.last();   # 3
a.first();  # 1
a.add(14);  # [1,2,3,14]
a.remove(1) # [2,3,14]
```

Nested lists
```html
make a = [[1,2,3],[4,5,6],[7,8,9]];
a[0][0] = 15;
make b = a[1][1];
print(a[0][0]); # prints 15 
print(b);       # prints 5
```

#### For Loop
- To loop through 1 to 3(including) use the below code. if you don't want to include the upper bound just remove `=`
  like `for 1..3`
- For loop had special index variable named `i` which can be used. it will be only available in the for loop scope.
```console
for 1..=3 {
    print(i);
}
```
- if you want to give another name to your index variable you can do it like below
```console
for 1..3 : specialIndex {
  print(specialIndex);
} 
```
You can also use for loop like below
```console
make a = ["Deneme","Test","Nlang"];
for x in a {
    print(x);
}
```

Nested for example with special index parameters.
```console
make a = [1,2,3];
make b = ["a","b","c"];

for 0..len(a) : x {
    for x..len(b) : y{
        print(a[x],b[y]);
    }
}

```

### Functions
```html
func fib(n){
    if(n<2){
        return n;
    }
    return fib(n-1) + fib(n-2);
}
make result = fib(20);
print(result);
```

### Built-in functions
- `len` function able to measure len of any iterable including strings
  ```console
  print(len("test"));  # 4
  print(len([1,2,3])); # 3 
  ```
- `time` function to measure time
  ```html
  make start = time()
  //some stuff
  print("Time passed",time()-time());
  ```

#### Objects

Objects are just hashmaps !

```html
make obj = { "key1":"value1"};
print(obj.key1); # value1
```

#### Error reporting

```html
make a = 2;
for x in a {
    print(x);
}
```
You will get the following error if you execute the code above

```console
make a = 2;
for x in a {
         ^
    print(x);
Error: variable a is not iterable. at line 2:10
```
