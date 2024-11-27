package com.github.synnerz.akutz.api.objects.state

class StateExp(private val left: IState<Any>) : StateVar<Boolean>(isTruthy(left.get())) {
    private var right: IState<Any>? = null
    private var cmpVal: Any? = null
    private var cmpList: List<*>? = null
    private var cmpCb1: ((Any) -> Boolean)? = null
    private var cmpCb2: ((Any, Any) -> Boolean)? = null
    private var op: Operator = Operator.IDENTITY
    private var dirt = true

    constructor(initialValue: Any) : this(StateVar(initialValue)) {}

    init {
        add(left)
    }

    private fun add(p: IState<Any>) {
        p.listen { -> dirty().get() }
    }

    fun dirty() = apply { dirt = true }

    override fun get(): Boolean {
        if (dirt) {
            dirt = false
            set(evaluate())
        }
        return super.get()
    }


    private fun evaluate(): Boolean {
        return when (op) {
            Operator.IDENTITY -> isTruthy(left.get())
            Operator.NOT -> !isFalsy(left.get())
            Operator.AND -> isTruthy(left.get()) && isTruthy(right!!.get())
            Operator.OR -> isTruthy(left.get()) || isTruthy(right!!.get())
            Operator.EQUALS -> left.get() == cmpVal
            Operator.NOTEQUALS -> left.get() != cmpVal
            Operator.EQUALSMULT -> cmpList!!.contains(left.get())
            Operator.CUSTOMUNARY -> cmpCb1!!.invoke(left.get())
            Operator.CUSTOMBINARY -> cmpCb2!!.invoke(left.get(), right!!.get())
        }
    }

    fun not(): StateExp {
        if (op != Operator.IDENTITY) return StateExp(left).not()
        op = Operator.NOT
        return dirty()
    }

    fun and(v: Any): StateExp = and(StateVar(v))
    fun and(v: IState<Any>): StateExp {
        if (op != Operator.IDENTITY) return StateExp(left).and(v)
        right = v
        add(v)
        op = Operator.AND
        return dirty()
    }

    fun or(v: Any): StateExp = or(StateVar(v))
    fun or(v: IState<Any>): StateExp {
        if (op != Operator.IDENTITY) return StateExp(left).or(v)
        right = v
        add(v)
        op = Operator.OR
        return dirty()
    }

    fun equalsval(t: Any): StateExp {
        op = Operator.EQUALS
        cmpVal = t
        return dirty()
    }

    fun notequals(t: Any): StateExp {
        op = Operator.NOTEQUALS
        cmpVal = t
        return dirty()
    }

    fun equalsmult(vararg t: Any): StateExp {
        op = Operator.EQUALSMULT
        cmpList = t.toList()
        return dirty()
    }

    fun customUnary(cb: (Any) -> Boolean): StateExp {
        op = Operator.CUSTOMUNARY
        cmpCb1 = cb
        return dirty()
    }

    fun customBinary(v: Any, cb: (Any, Any) -> Boolean): StateExp = customBinary(StateVar(v), cb)
    fun customBinary(v: IState<Any>, cb: (Any, Any) -> Boolean): StateExp {
        if (op != Operator.IDENTITY) return StateExp(left).customBinary(v, cb)
        right = v
        add(v)
        op = Operator.CUSTOMBINARY
        cmpCb2  = cb
        return dirty()
    }

    companion object {
        @JvmStatic
        private fun isFalsy(value: Any?): Boolean {
            return when (value) {
                is Boolean -> value == false
                is Number -> value == 0
                is String -> value.isEmpty()
                null -> true
                else -> false
            }
        }

        @JvmStatic
        private fun isTruthy(value: Any?) = !isFalsy(value)

        enum class Operator(val code: Int) {
            IDENTITY(1),
            NOT(3),
            AND(2),
            OR(4),
            EQUALS(5),
            NOTEQUALS(7),
            EQUALSMULT(9),
            CUSTOMUNARY(11),
            CUSTOMBINARY(6);
        }
    }
}
