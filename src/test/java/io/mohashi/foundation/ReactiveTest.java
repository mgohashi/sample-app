package io.mohashi.foundation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.AssertSubscriber;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;

public class ReactiveTest {

    @Test
    public void testBasicUni() {
        Uni<String> someUni = Uni.createFrom().item("test")
            .onItem().transform(m -> m + " good");

        UniAssertSubscriber<String> subs = 
        someUni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        subs
            .assertCompleted().assertItem("test good");
    }

    @Test
    public void testBasicMulti() {
        Multi<String> someMulti = Multi.createFrom().items("a","b","c")
            .onItem().transform(m -> m + "-item");
        AssertSubscriber<String> subs = someMulti
            .subscribe().withSubscriber(AssertSubscriber.create(3));
        
        subs.assertCompleted().assertItems("a-item", "b-item", "c-item");
    }

    @Test
    public void testCollectMulti() {
        Multi<String> someMulti = Multi.createFrom().items("a","b","c")
            .onItem().transform(m -> m + "-item");
        Uni<List<String>> uni = someMulti.collect().asList();
        UniAssertSubscriber<List<String>> subs = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());
        
        subs.assertCompleted().assertItem(Arrays.asList("a-item", "b-item", "c-item"));
    }

    @Test
    public void testUniLog() {
        Uni<String> someUni = Uni.createFrom().item("a")
            .map(i -> i + "a")
            .log();
        UniAssertSubscriber<String> subs = someUni
            .subscribe().withSubscriber(UniAssertSubscriber.create());
        
            subs.assertCompleted().assertItem("aa");
    }

    @Test
    public void testUniMap() {
        Uni<String> someUni = Uni.createFrom().item("hello")
            .map(i -> i + " world");
        UniAssertSubscriber<String> subs = someUni
            .subscribe().withSubscriber(UniAssertSubscriber.create());
        
            subs.assertCompleted().assertItem("hello world");
    }

    @Test
    public void testUniFlatMap() {
        Uni<Integer> someUni = Uni.createFrom().item("hello")
            .flatMap(i -> Uni.createFrom().item(i.length()));
        UniAssertSubscriber<Integer> subs = someUni
            .subscribe().withSubscriber(UniAssertSubscriber.create());
        
            subs.assertCompleted().assertItem(5);
    }

    @Test
    public void testMultiCollectList() {
        List<Character> myItems = Multi.createFrom().items("a","b","c")
            .flatMap(i -> Multi.createFrom().items(Character.valueOf(i.charAt(0))))
            .collect().asList().await().indefinitely();
        
        assertThat(myItems, equalTo(
            Arrays.asList(
                Character.valueOf('a'),
                Character.valueOf('b'),
                Character.valueOf('c'))
                ));
    }

    private List<String> cache = Arrays.asList("a", "b", "c", "d");

    private Multi<Character> getCode(String value) {
        Integer index = cache.indexOf(value) + 1;
        return Multi.createFrom().items(
            Character.valueOf(value.charAt(0)),
            index.toString().toCharArray()[0]);
    }

    @Test
    public void testMultiConcatMap() {
        List<Character> myItems = Multi.createFrom()
            .items(cache.toArray(new String[0]))
            .concatMap(this::getCode)
            .collect().asList().await().indefinitely();
        
        assertThat(myItems, equalTo(
            Arrays.asList(
                Character.valueOf('a'),
                Character.valueOf('1'),
                Character.valueOf('b'),
                Character.valueOf('2'),
                Character.valueOf('c'),
                Character.valueOf('3'),
                Character.valueOf('d'),
                Character.valueOf('4')
                )));
    }

    @Test
    public void testMultiNullValues() {
        List<String> items = Arrays.asList("1", null, "3", null, "5");

        UniAssertSubscriber<List<String>> subs =
            Multi.createFrom().items(items.stream())
                .collect().asList()
                // .onFailure().invoke(t -> t.printStackTrace())
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        subs.assertFailedWith(NullPointerException.class, 
            "The stream iterator produced `null`");
    }

    @Test
    public void testMultiFilter() {
        AssertSubscriber<Integer> subs = Multi.createFrom()
            .range(1,10)
                .select().where(i -> i % 2 == 0)
                .subscribe().withSubscriber(AssertSubscriber.create(10));
        subs.assertCompleted().assertItems(2,4,6,8);
    }

    @Test
    public void testMultiMerging() {
        Multi<Integer> m1 = Multi.createFrom().range(1, 10);
        Multi<Integer> m2 = Multi.createFrom().range(10, 20);

        Multi<Integer> merged = Multi.createBy().merging().streams(m1, m2);


        AssertSubscriber<Integer> subs = merged.subscribe()
            .withSubscriber(AssertSubscriber.create(20));

        subs.assertCompleted()
            .assertItems(IntStream.range(1, 20)
                .boxed().toList().toArray(Integer[]::new));
    }

    @Test
    public void testMultiConcatenating() {
        Multi<Integer> m1 = Multi.createFrom().range(1, 10);
        Multi<Integer> m2 = Multi.createFrom().range(10, 20);

        Multi<Integer> concatenated = 
            Multi.createBy().concatenating().streams(m1, m2);
        
        AssertSubscriber<Integer> subs = concatenated.subscribe()
            .withSubscriber(AssertSubscriber.create(20));
        
        subs.assertCompleted()
            .assertItems(IntStream.range(1, 20)
                .boxed().toList().toArray(Integer[]::new));
    }
}