package com.dylan.coroutine

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.*
import com.dylan.coroutine.databinding.ActivityCoroutineBinding
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.*
import kotlin.system.measureTimeMillis

class CoroutineActivity : AppCompatActivity() {
    private val TAG = "TAG"
    private lateinit var binding: ActivityCoroutineBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_coroutine)
        initView()
        MutableStateFlow(1)
    }

    private fun initView() {
        binding.run {
            // 测试协程基本用法（test~test7）
            /**
             * flow是冷数据流：
             * 1.为每个收集器（消费者）单独启动
             * 2.消费者消费时，生产者才开始生产
             * 3.所有数据流收集（collect）完毕后，flow会自动销毁，后续再往该flow中放进新值，collect也不会收到新值
             */
            text1Mb.setOnClickListener {
//                test()
//                test2()
//                test3()
//                test4()
//                test5()
//                test6()
                test7()
            }

            /**
             * sharedFlow是热流，stateFlow继承自sharedFlow：
             * 1.独立于订阅者之外存在，以广播的形式向订阅者发送数据；
             * 2.每次发送的都是最新数据，订阅者可能丢失为消费的较老的数据；
             * 3.流永远不会正常完成；
             *
             * statedFlow用法
             */
            text2Mb.setOnClickListener {
//                test11()
                test12()
            }

            /**
             * sharedFlow用法
             */
            text3Mb.setOnClickListener {
//                test21()
                test22()
            }
        }
    }

    /**
     * 内存泄露问题：stateFlow、sharedFlow不会像livedata一样自动观察生命周期进行解除注册，即使view不可见也会处理事件，从而可能导致应用崩溃，需要使用repeatOnLifecycle()函数观察lifecycle生命周期
     */
    private fun test22() {
        lifecycleScope.launch {
            /*repeatOnLifecycle(Lifecycle.State.STARTED) {

            }*/
//            mutableListOf(1, 2, 3).asFlow().shareIn(this, SharingStarted.Lazily, replay = 2)
        }
    }

    private fun test21() {
        val viewModel = ViewModelProvider(this).get<TestSharedFlowVm>()
        viewModel.viewModelScope.launch {
            launch {
                viewModel.state.collect {
                    Log.i(TAG, "test21: 第一个collect:$it")
                }
            }
            launch {
                delay(3000)
                viewModel.state.collect {
                    Log.i(TAG, "test21: 第二个222collect:$it")
                }
            }
        }
        viewModel.download()
    }

    class TestSharedFlowVm : ViewModel() {
        private val _state = MutableSharedFlow<Int>(2)
        val state: SharedFlow<Int> get() = _state
        fun download() {
            for (i in 0 until 5) {
                viewModelScope.launch(Dispatchers.IO) {
                    delay(i * 100L)
                    _state.emit(i)
                }
            }
        }
    }

    /******************************************************************stateFlow用法************************************************************************/

    /**
     * stateFlow使用
     */
    private fun test12() {
        val viewModel = ViewModelProvider(this).get<TestFlowViewModel>()
        viewModel.viewModelScope.launch {
            try {
                viewModel.stateFlow.collect {
                    Log.i(TAG, "test12: 线程：${Thread.currentThread().name}\tstate:$it")
                    if (it == 3) {
                        throw NullPointerException("终止stateFlow收集")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            viewModel.nameFlow.collect {
                Log.i(TAG, "test12: 线程：${Thread.currentThread().name}\tstate:$it")
            }
        }
        viewModel.download()
    }

    class TestFlowViewModel : ViewModel() {
        private val _state = MutableStateFlow(0)
        val stateFlow: StateFlow<Int> get() = _state

        private val _name = MutableStateFlow("nameFlow")
        val nameFlow: StateFlow<String> get() = _name

        fun download() {
            for (state in 0 until 5) {
                viewModelScope.launch(Dispatchers.IO) {
                    delay((200 * state.toLong()))
                    _state.value = state
                }
            }
        }
    }

    /**
     * channel用法、优势及弊端
     * channel是一个非阻塞的原始发送者之间的对话沟通，通俗的说就是一个数据管道，也是一个热流
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun test11() {
        /* runBlocking {
             var flow1 = (0 until 3).asFlow()
             flow1.collect {
                 Log.i(TAG, "test11: collect:$it")
             }
             flow1 = (4 until 6).asFlow()
             flow1.collect {
                 Log.i(TAG, "test11: collect:$it")
             }
         }*/
        /*runBlocking {
            val channel = Channel<Int>()
            launch {
                if (!channel.isClosedForSend) {
                    for (i in 0 until 5) {
                        channel.send(i)
                    }
                    channel.close()
                }
            }
            launch {
                delay(1000)
                if (!channel.isClosedForSend) {
                    channel.send(666)
                    channel.send(999)
                }
            }
            while (true) {
                if (!channel.isClosedForReceive) {
                    Log.i(TAG, "test11: receive:${channel.receive()}")
                } else {
                    break
                }
            }

            Log.i(TAG, "test11: runBlocking done")
        }*/

        runBlocking {
            val squares = produce { for (i in 0 until 5) send(i) }
            squares.consumeEach {
                Log.i(TAG, "test11: receive:$it")
            }
            Log.i(TAG, "test11: done")
        }
    }

    /******************************************************************协程基本用法************************************************************************/

    /**
     * 缓冲操作符
     * buffer：新开一个协程，单独执行buffer操作符之前的代码，以节省buffer之前的总耗时
     */
    private fun test7() {
        lifecycleScope.launch {
            measureTimeMillis {
                flow {
                    for (i in 0 until 3) {
                        delay(100)
                        emit(i)
                    }
                }.buffer().collect {
                    delay(300)
                    Log.i(TAG, "test7: collect:$it")
                }
            }.let {
                Log.i(TAG, "test7: time:$it ms")
            }
        }
    }

    /**
     * 末端操作符
     * collect：最基本的末端操作符
     * toList：将流转换为list集合
     */
    private fun test6() {
        lifecycleScope.launch {
            (0 until 9).toList().apply {
                Log.i(TAG, "test6: apply:$this")
            }.asFlow().filter {
                it < 6
            }.map {
                "${it}元"
            }.toList().let {
                Log.i(TAG, "test6: toList:$it")
            }
        }
    }

    /**
     * 限制操作符
     * take：限制发射次数，当发射次数大于等于count值时，抛出终止flow的异常以终止协程
     * takeWhile：类似于filter，不同的是，在不满足条件时，filter会自动跳过，进行下一个，但takeWhile会中断后续操作
     * drop：与take恰好相反，它是丢弃掉指定数量的流后，执行后续的流
     */
    private fun test5() {
        lifecycleScope.launch {
            // take
            /*(0 until 5).asFlow().take(3).collect {
                Log.i(TAG, "test5: collect:$it")
            }*/

            // takeWhile
            /*(0 until 5).asFlow().map {
                delay(100)
                Log.i(TAG, "test5: map:$it")
                it
            }.takeWhile {
                it <= 2
            }.collect {
                Log.i(TAG, "test5: collect:$it")
            }*/

            // drop
            (0 until 5).asFlow().drop(2).collect {
                Log.i(TAG, "test5: collect:$it")
            }

        }
    }

    /**
     * 转换操作符：转换数据，最通用的一种为transform，可以模仿简单的转换。常用的有map、filter、zip、combine、flatMapConcat、flatMapMerge、flatMapLatest等等
     * map：基于transform，用于转换值或值的类型
     * filter： 过滤操作符，仅保留满足条件的值
     * filterNot：过滤操作符，仅保留不满足条件的值
     * zip：组合操作符，组合拼接两个flow的值，其中一个流动完毕时，另一个也随之结束
     */
    private fun test4() {
        lifecycleScope.launch {
            // transform
//            (1..3).asFlow().transform {
//                emit(it.toString())
//                emit("transform $it")
//            }.collect { value ->
//                Log.i(TAG, "test4: collect:$value")
//            }
            /* flow {
                 emit(1)
             }.transform {
                 emit(it.toString())
             }.collect {

             }*/

            // map
            /*flow {
                emit(1)
            }.map {
                Log.i(TAG, "test4: 第一次转换")
                it * 5
            }.map {
                Log.i(TAG, "test4: 第二次转换")
                "map $it"
            }.mapNotNull {
                it
            }.collect {
                Log.i(TAG, "test4: 最终值：$it")
            }*/

            // filter
            /*(0 until 5).asFlow().filter {
                it < 2
            }.collect {
                Log.i(TAG, "test4: collect:$it")
            }*/

            // filterNot
            /* (0 until 5).asFlow().filterNot {
                 it < 2
             }.collect {
                 Log.i(TAG, "test4: collect:$it")
             }*/

            // zip
            val flow1 = (0 until 5).asFlow()
            val flow2 = flowOf("zero", "one", "two", "three")
            flow2.zip(flow1) { i, s ->
                "$i$s"
            }.collect {
                Log.i(TAG, "test4: collect:$it")
            }
        }
    }

    /**
     * 异常操作符：发生异常后调用，只对上游流负责，下游流异常了不负责
     * catch：相当于try catch代码块，用于捕获异常，可以在异常时对异常进行处理，此时程序不会崩溃
     */
    private fun test3() {
        lifecycleScope.launch {
            flow<Int> {
                emit(1)
                throw NullPointerException("测试协程异常")
            }.onStart {
                Log.i(TAG, "test2: onStart")
            }.onEach {
                Log.i(TAG, "test2: onEach")
            }.onEmpty {
                Log.i(TAG, "test2: onEmpty")
            }.onCompletion {
                /* if (it== null){

                 }*/
                Log.i(TAG, "test2: onCompletion")
            }.catch { cause: Throwable ->
                cause.printStackTrace()
                emit(100)
            }.collect {
                Log.i(TAG, "test2: collect $it")
            }
        }
    }

    /**
     *流程操作符：又叫过渡操作符，用于区分流程执行到某一阶段
     * onStart：在上游流启动之前调用
     * onEach：在上游流的每个值被下游发出之前调用
     * onCompletion：在流完成、取消后调用，并将取消异常或失败原因作为参数传递
     */
    private fun test2() {
        lifecycleScope.launch {
            flow {
                emit(1)
            }.onStart {
                Log.i(TAG, "test2: onStart")
            }.onEach {
                Log.i(TAG, "test2: onEach")
            }.onEmpty {
                Log.i(TAG, "test2: onEmpty")
            }.onCompletion {
                Log.i(TAG, "test2: onCompletion")
            }.collect {
                Log.i(TAG, "test2: collect")
            }
        }
    }

    private fun test() {
        /*val job = lifecycleScope.launch {
            Log.i(TAG, "test:进入job 线程：${currentCoroutineContext()}")
            flow<Int> {
                Log.i(TAG, "test:进入flow 线程：${currentCoroutineContext()}")
                for (i in 1..3) {
                    emit(i)
                    Log.i(TAG, "进入for test: 已发射value$i")
                    delay(1000)
                }
            }.flowOn(Dispatchers.IO).collect {
                Log.i(TAG, "value：$it")
                Log.i(TAG, "test:进入flowOn 线程：${currentCoroutineContext()}")
            }

            Log.i(TAG, "test: 已执行收集函数")
            *//*(1..3).asFlow().collect {
                Log.i(TAG, "value：$it")
            }*//*
            *//*flowOf(1, 2, 3).collect {
                Log.i(TAG, "value：$it")
            }*//*
        }*/
//        job.cancel()
        lifecycleScope.launch {
            flow {
                for (i in 1..3) {
                    Log.i(TAG, "test: 发射 for:${currentCoroutineContext()} value:$i")
                    delay(1000)
                    emit(i)
                }
            }.flowOn(Dispatchers.IO)
                .onStart { }
                .onEach { }


                .map {
                    Log.i(TAG, "test: map:${currentCoroutineContext()} value:$it")
                    it
                }/*.flowOn(Dispatchers.Default)*/
                .map {
                    Log.i(TAG, "test: map2:${currentCoroutineContext()} value:$it")
                    it
                }.flowOn(Dispatchers.Default)
                .collect {
                    Log.i(TAG, "test: 接收 collect:${currentCoroutineContext()} value:$it")
                }
        }
    }
}