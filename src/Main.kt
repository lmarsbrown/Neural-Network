fun main()
{
    val data = sequence<Pair<Matrix,Matrix>> {
        while(true)
        {
            val input = Matrix(1,1);
            input.setItem(0,0,1.0);
            val output = Matrix(1,1);
            output.setItem(0,0,1.0);
            yield(Pair(input,output))
        }
    }
    console.log("Test")

    var net = Network(arrayOf(1,2,1));
    net.compute(data.iterator().next().first)
    net.cost(data.iterator().next().second);
    net.fit(data,1.0,10,5);
    net.compute(data.iterator().next().first).log();
    net.cost(data.iterator().next().second);
    //console.log(net.cost(data.iterator().next().second));
}