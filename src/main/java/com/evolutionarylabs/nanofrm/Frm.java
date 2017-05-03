package com.evolutionarylabs.nanofrm;

import com.evolutionarylabs.nanofrm.dto.Endereco;
import com.evolutionarylabs.nanofrm.dto.Pessoa;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.function.Function;

/**
 * Created by cleberzanella on 13/04/17.
 */
public class Frm {

    public static void main(String... args){


        final int id = 1;
        String log = "dfd";
        Queryable<Pessoa> q2 = query((b, f, t) -> b.from(Pessoa.class)
                .join(Endereco.class).on( (p, e) -> f.and(f.eq(e.getPessoaId(), p.getId()), e.isPrincipal()))
                .where( (p, e) -> f.eq(p.getId(), id))
                .where( (p, e) -> f.eq(f.lower(e.getLogradouro()), f.lower(log)))
        );
        System.out.println(q2.getAST());

        // query com join especial: Pessoa, Endereco principal
        // SELECT p.*, e.* FROM Pessoa p LEFT OUTER JOIN Endereco e ON(e.pessoaId = p.id AND e.principal)
        // WHERE e.principal ORDER BY p.nome ASC, logradouro DESC LIMIT 10,5
        Queryable<Pessoa> q3 = query( (b,f,t) -> b.from(Pessoa.class)
                .join(Endereco.class)
                .on( (p, e) -> f.and(f.eq(e.getPessoaId(), p.getId()), e.isPrincipal()))
                .where( (p, e) -> e.isPrincipal())
                .order((p, e) -> f.by(f.asc(p.getNome()), f.desc(e.getLogradouro())))
                .limit(10, 5)

        );
        System.out.println(q3.getAST());

//        // PHP
//        Queryable $q = FRM::query(function(QueryBuilder $b, Functions $f) {
//                return $b->from(Pessoa::CLASS_NAME)
//                ->join(Endereco::CLASS_NAME)
//                    ->on( function(Pessoa $p, Endereco $e) use($f) {
//                    return $f->and($f->eq($e->getPessoaId(), $p->getId(), $e->isPrincipal()));
//                })
//                ->where( function(Pessoa $p, Endereco $e) use($f) {
//                    return $e->isPrincipal();
//                })
//                ->order( function(Pessoa $p, Endereco $e) use($f) {
//                    $f->by($f->asc($p->getNome()), $f->desc($e->getLogradouro());
//                })
//                ->limit(10,5);
//            });
//
//        // PHP short closures
//        FRM::query((QueryBuilder $b, Functions $f) ~> {
//            return $b->from(Pessoa::CLASS_NAME)
//                ->join(Endereco::CLASS_NAME)
//                    ->on( (Pessoa $p, Endereco $e) use($f) ~> $f->and($f->eq($e->getPessoaId(), $p->getId(), $e->isPrincipal())) )               })
//                ->where( (Pessoa $p, Endereco $e) use($f) ~> $e->isPrincipal())
//                ->order( (Pessoa $p, Endereco $e) use($f) ~> $f->by($f->asc($p->getNome()), $f->desc($e->getLogradouro())) )
//                ->limit(10,5);
//        });
//
//
//        // C#
//        Queryable<Pessoa> q3 = FRM.Query( (b) => b.From<Pessoa>()
//                .Join<Endereco>()
//                  .On( (p, e) => e.PessoaId == p.Id && e.Principal )
//                .Where( (p, e) => e.Principal)
//                .Order((p, e) => f.By(f.Asc(p.Nome), f.Desc(e.Logradouro)))
//                .Limit(10, 5) );



    }

    public static <T> Queryable<T> query(QueryBuilderFunction<T> fn){

        CallContext callContext = new CallContext();

        return fn.build(new QueryBuilder<T>(callContext), new Functions(callContext), new Transformers());
    }

    @FunctionalInterface
    public interface QueryBuilderFunction<T> {
        Queryable<T> build(QueryBuilder<T> b, Functions f, Transformers t);
    }

    public static class QueryBuilder<T> {

        protected CallContext callContext;

        public QueryBuilder(CallContext callContext) {
            this.callContext = callContext;
        }

        public Queryable<T> from(Class<T> domainClass) {

            if(callContext.isReadingTree()){

                Expr[] callsAndValues = callContext.verifyParams(new Class[] { }, new Object[] { });
                callContext.registerCall(new Expr(Expr.CALL, Class.class, "from_" + domainClass.getSimpleName(), callsAndValues));
            }

            return new Queryable<T>(callContext, domainClass); // TODO internally call .select() (the default projection in AST)
        }
    }


    public static class Queryable<T> {

        protected CallContext callContext;
        private Class<T> domainClass1;

        public Queryable(CallContext callContext, Class<T> domainClass) {
            this.callContext = callContext;
            this.domainClass1 = domainClass;
        }

        public <T2> Queryable2<T, T2> join(Class<T2> joinClass) {

            if(callContext.isReadingTree()){

                Expr[] callsAndValues = callContext.verifyParams(new Class[] { }, new Object[] { });
                callContext.registerCall(new Expr(Expr.CALL, Class.class, "join_" + joinClass.getSimpleName(), callsAndValues));
            }

            return new Queryable2<T, T2>(callContext, domainClass1, joinClass);
        }

        public String getSql() {
            // TODO
            return "SELECT * FROM " + domainClass1.getSimpleName();
        }

        public String getAST(){
            StringBuffer sb = new StringBuffer();

            for(Expr expr : callContext.callStack){
                getAST(expr, sb, 0);
            }

            return sb.toString();
        }

        private void getAST(Expr expr, StringBuffer sb, int level){

            sb.append('\n');
            for(int i = level; i > 0; i--){
                sb.append('\t');
            }

            sb.append(expr.getType() == Expr.CALL ? "call:" : "value:").append("<").append(expr.getReturType().getSimpleName()).append(">");
            sb.append(expr.getName());

            for(Expr child : expr.childrem){
                getAST(child, sb, level + 1);
            }
        }
    }

    public static class Queryable2<T1, T2> extends Queryable<T1>{

        private Class<T2> domainClass2;

        public Queryable2(CallContext callContext, Class<T1> domainClass1, Class<T2> domainClass2) {
            super(callContext, domainClass1);
            this.domainClass2 = domainClass2;
        }

        public Queryable2<T1, T2> on(Predicate2<T1, T2> pred2){

            if(callContext.isReadingTree()){

                pred2.apply(callContext.proxy(super.domainClass1), callContext.proxy(domainClass2));

                Expr[] callsAndValues = callContext.verifyParams(new Class[] { Boolean.class }, new Object[] { true });
                callContext.registerCall(new Expr(Expr.CALL, Boolean.class, "on", callsAndValues));
            }

            return this;
        }

        public Queryable2<T1, T2> where(Predicate2<T1, T2> pred2){

            if(callContext.isReadingTree()){

                pred2.apply(callContext.proxy(super.domainClass1), callContext.proxy(domainClass2));

                Expr[] callsAndValues = callContext.verifyParams(new Class[] { Boolean.class }, new Object[] { true });
                callContext.registerCall(new Expr(Expr.CALL, Boolean.class, "where", callsAndValues));
            }

            return this;
        }

        public Queryable2<T1, T2> order(Action2<T1, T2> action2){

            if(callContext.isReadingTree()){

                action2.apply(callContext.proxy(super.domainClass1), callContext.proxy(domainClass2));

                Expr[] callsAndValues = callContext.verifyParams(new Class[] { Boolean.class }, new Object[] { true });
                callContext.registerCall(new Expr(Expr.CALL, Boolean.class, "order", callsAndValues));
            }

            return this;
        }

        public Queryable2<T1, T2> limit(int take, Integer limit) {
            return this;
        }
    }

    @FunctionalInterface
    public interface Predicate2<T1, T2> {
        boolean apply(T1 t1, T2 t2);
    }

    @FunctionalInterface
    public interface Action2<T1, T2> {
        void apply(T1 t1, T2 t2);
    }


    public static class Sort<T> {

        public static final Sort<?> NULL = new Sort<>();

    }

    public static class Functions {

        private CallContext callContext;

        public Functions(CallContext callContext) {
            this.callContext = callContext;
        }

        public boolean eq(int v1, int v2) {

            if(callContext.isReadingTree()){
                Expr[] callsAndValues = callContext.verifyParams(new Class[] { int.class, int.class }, new Object[] { v1, v2 });
                callContext.registerCall(new Expr(Expr.CALL, Boolean.class, "fn_eq", callsAndValues));

                return true; // true for continuing execution and AST construction
            }

            return v1 == v2;
        }

        public boolean eq(String v1, String v2) {

            if(callContext.isReadingTree()){
                Expr[] callsAndValues = callContext.verifyParams(new Class[] { String.class, String.class }, new Object[] { v1, v2 });
                callContext.registerCall(new Expr(Expr.CALL, Boolean.class, "fn_eq", callsAndValues));

                return true;
            }

            return v1 == v2;
        }

        public boolean and(boolean exp1, boolean exp2) {

            if(callContext.isReadingTree()){
                Expr[] callsAndValues = callContext.verifyParams(new Class[] { boolean.class, boolean.class }, new Object[] { exp1, exp2 });
                callContext.registerCall(new Expr(Expr.CALL, Boolean.class, "fn_and", callsAndValues));

                return true;
            }

            return exp1 && exp2;
        }

        public String lower(String str) {

            if(callContext.isReadingTree()){
                Expr[] callsAndValues = callContext.verifyParams(new Class[] { String.class }, new Object[] { str });
                callContext.registerCall(new Expr(Expr.CALL, String.class, "fn_lower", callsAndValues));

                return CallContext.DEFAULT_STRING;
            }

            return str.toLowerCase();
        }

        public void by(Sort<?>... sorts){

            if(callContext.isReadingTree()){

                Class[] returnTypes = new Class[sorts.length];
                Object[] returnValues = new Object[sorts.length];

                for(int i = 0; i < sorts.length; i++){
                    returnTypes[i] = Boolean.class;
                    returnValues[i] = true;
                }

                Expr[] callsAndValues = callContext.verifyParams(returnTypes, returnValues);
                callContext.registerCall(new Expr(Expr.CALL, Boolean.class, "by", callsAndValues));
            }

            // TODO Sort collection by comparators
        }

        public <T> Sort<T> asc(String strValue){

            if(callContext.isReadingTree()){
                Expr[] callsAndValues = callContext.verifyParams(new Class[] { String.class }, new Object[] { strValue });
                callContext.registerCall(new Expr(Expr.CALL, String.class, "fn_asc", callsAndValues));

                return (Sort<T>) Sort.NULL;
            }

            return new Sort<T>();
        }

        public <T> Sort<T> desc(String strValue){

            if(callContext.isReadingTree()){
                Expr[] callsAndValues = callContext.verifyParams(new Class[] { String.class }, new Object[] { strValue });
                callContext.registerCall(new Expr(Expr.CALL, String.class, "fn_desc", callsAndValues));

                return (Sort<T>) Sort.NULL;
            }

            return new Sort<T>();
        }

    }

    public static class Transformers {

    }

    public static class CallContext {

        public final static String DEFAULT_STRING = "/*-";

        private Stack<Expr> callStack = new Stack<>();
        private Map<Class<?>, Object> propertiesDefaulValues = new HashMap<>();

        {
            Class<?>[] classes = new Class[] { int.class };

            for(Class<?> cls : classes){
                propertiesDefaulValues.put(cls, Mapper.PrimitiveDefaults.getDefaultValue(cls));
            }

            propertiesDefaulValues.put(Integer.class, 0);
            propertiesDefaulValues.put(String.class, DEFAULT_STRING);

        }

        public <T> T proxy(Class<T> clazz){

            return DynamicProxy.proxy(clazz, new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                    if(isReadingTree()){

                        Expr[] callsAndValues = verifyParams( method.getParameterTypes(), args ); // only for getters

                        registerCall(new Expr(Expr.CALL, method.getReturnType(), "prop_" + clazz.getSimpleName() + "_" + method.getName(), callsAndValues));
                    }

                    return method.getReturnType() == String.class ? DEFAULT_STRING : Mapper.PrimitiveDefaults.getDefaultValue(method.getReturnType()); // never return null, ever default
                }
            }, DynamicProxy.BYTE_BUDDY_FACTORY);

        }

        public boolean isReadingTree(){
            return true;
        }

        public Expr[] verifyParams(Class[] paramTypes, Object[] paramValues){

            // remove from stack
            Expr[] paramsExpr = new Expr[paramTypes.length];

            // reversed iteration
            for (int i = paramTypes.length - 1; i >= 0; i--){

                Class<?> retType = paramTypes[i];
                Expr expr;

                if(propertiesDefaulValues.containsKey(retType) && ! propertiesDefaulValues.get(retType).equals(paramValues[i])){
                    // its a value
                    expr = new Expr(Expr.VALUE, retType, " " + paramValues[i], new Expr[0]);
                } else {

                    // its a call
                    expr = callStack.pop();
                }

                paramsExpr[i] = expr;

                // TODO : verify type incompatibilities
            }

            return paramsExpr;
        }

        public void registerCall(Expr expr){
            // add at stack
            callStack.push(expr);
        }

    }

    public static class Expr {

        public static final int CALL = 1;
        public static final int VALUE = 2;

        private int type;
        private Class<?> returType;
        private String name;
        private Expr[] childrem;

        public Expr(int type, Class<?> returType, String name, Expr[] childrem) {
            this.type = type;
            this.returType = returType;
            this.name = name;
            this.childrem = childrem;
        }

        public int getType() {
            return type;
        }

        public Class<?> getReturType() {
            return returType;
        }

        public String getName() {
            return name;
        }
    }

}
