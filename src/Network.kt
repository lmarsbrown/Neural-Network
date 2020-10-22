import kotlin.math.E
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.pow
import kotlin.random.Random

class Network(layers:Array<Int>){
    var nodeLayers = Array<Matrix>(layers.size, {i : Int -> Matrix(1,layers.get(i))});
    var weights = Array<Matrix>(layers.size-1,{i ->
        Matrix(layers.get((i)),layers.get((i+1)),{ _ -> Random.nextDouble(-1.0,1.0)});
    });
    var biases = Array<Matrix>(layers.size-1,{i : Int ->
        Matrix(1,layers.get(i+1),{ _ -> Random.nextDouble(-1.0,1.0)});
    });
    var zS = Array<Matrix>(layers.size, {i : Int -> Matrix(1,layers.get(i))});
    fun compute(input:Matrix):Matrix
    {
        if(input.height != nodeLayers.get(0).height)
        {
            throw IllegalArgumentException("Input is wrong size");
        }
        nodeLayers.set(0,input);
        for(i in 0 until nodeLayers.size-1)
        {
            var nextLayer = weights.get(i).mul(nodeLayers.get(i));
            nextLayer.add(biases.get(i))
            zS[i] = nextLayer;
            nextLayer.runForEach({x,y -> activation(nextLayer.getItem(x,y))});
            nodeLayers.set(i+1,nextLayer);

        }
        return nodeLayers.get(nodeLayers.size-1);
    }
    private fun sigmoid(x:Double):Double
    {
        return 1/(1+ exp(-x));
    }

    private fun sigmoidDer(x:Double):Double
    {
        return (1+ exp(-x)).pow(-2)* exp(-x);
    }

    private fun activation(x:Double):Double
    {
        return sigmoid(x);
    }
    private fun activationDer(x:Double):Double
    {
        return sigmoidDer(x);
    }

    private fun outErr(goal: Matrix,stepSize:Double):Matrix
    {
        var out = this.nodeLayers.last();
        out.subtract(goal);
        var actDerZ = zS.last().clone();
        actDerZ.runForEach({
            x:Int,y:Int -> activationDer(actDerZ.getItem(x,y));
        } );
        out.mulElementwise(actDerZ);
        out.scale(-stepSize);
        return out;
    }

    private fun generalErr(layer:Int,nextErr:Matrix):Matrix
    {
        var out = this.weights[layer].getTransposeClone();
        out = out.mul(nextErr);


        var actDerZ = zS[layer-1].clone();
        actDerZ.runForEach({
            x:Int,y:Int -> activationDer(actDerZ.getItem(x,y));
        } );
        out.mulElementwise(actDerZ);
        return out;
    }

    private fun weightDer(layer:Int,err:Matrix):Matrix
    {
        var out = nodeLayers[layer-1].clone();
        out.mul(err);
        return out;
    }
    public fun backprop(goal: Matrix,stepSize: Double):Pair<Array<Matrix>,Array<Matrix>>
    {
        var errs = Array<Matrix>(this.nodeLayers.size-1,{ _: Int ->
            Matrix(1,1)
        });
        for(i in 0 until errs.size)
        {
            if(i == 0)
            {
                errs[0] = outErr(goal,stepSize);
            }
            else
            {
                errs[i] = generalErr(nodeLayers.size-1-i,errs[i-1]);
            }
        }
        var weights = Array<Matrix>(this.weights.size,{i : Int ->
            errs[this.nodeLayers.size-1-(i+1)].genMulTable(nodeLayers[i].clone());
        });

        return Pair(errs,weights);
    }

    private fun addSteps(s1:Pair<Array<Matrix>,Array<Matrix>>,s2:Pair<Array<Matrix>,Array<Matrix>>)
    {
        for(i in 0 until s1.first.size)
        {
            s1.first[i].add(s2.first[i])
        }
        for(i in 0 until s1.second.size)
        {
            s1.second[i].add(s2.second[i])
        }
    }

    private fun addStepToNet(step:Pair<Array<Matrix>,Array<Matrix>>)
    {

        for(i in 0 until step.first.size)
        {
            this.biases[this.biases.size-i-1].add(step.first[i])
        }
        for(i in 0 until step.second.size)
        {
            this.weights[i].add(step.second[i])
        }
    }

    public fun fit(data:Sequence<Pair<Matrix,Matrix>>,learningRate:Double,batchSize:Int,batches:Int)
    {
        for(i in 0 until batches)
        {
            this.addStepToNet(this.runBatch(batchSize,data,learningRate));
        }
    }

    public fun runBatch(bSize:Int,data: Sequence<Pair<Matrix,Matrix>>,stepSize:Double):Pair<Array<Matrix>,Array<Matrix>>
    {
        var d = data.iterator().next();
        this.compute(d.first);
        var step = this.backprop(d.second,stepSize);
        for(i in 0 until bSize - 1)
        {
            d =  data.iterator().next();
            this.compute(d.first);
            this.addSteps(step,this.backprop(d.second,stepSize));
        }
        return step;
    }


    public fun cost(goal:Matrix):Double
    {
        var costMat = this.nodeLayers.last().clone();
        var out:Double = 0.0;
        costMat.subtract(goal);
        costMat.runForEach({x,y ->
            console.log(costMat.getItem(x,y).pow(2));
            out.plus(costMat.getItem(x,y).pow(2));
        });
        return out;
    }
}