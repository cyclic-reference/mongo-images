package io.acari.images.flux;

import io.acari.images.mono.MonoSinkHelper;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;

public class IterableFlux<T> {
  private final Queue<T> itemBuffer = new LinkedList<>();
  private final Queue<MonoSinkHelper<T>> callables = new LinkedList<>();
  private final Disposable disposable;
  private boolean complete = false;

  public IterableFlux(Flux<T> source) {
    Flux<T> messaged = Flux.create(stringFluxSink ->
        source.subscribe(sourceItem -> emitNextItem(stringFluxSink, sourceItem),
        this::accept,
        this::run));
    disposable = messaged.subscribe();
  }

  public void dispose() {
    disposable.dispose();
    callables.forEach(MonoSinkHelper::success);
  }

  public Mono<T> onNext() {
    if (complete && itemBuffer.isEmpty()) {
      return Mono.empty();
    } else if (itemBuffer.isEmpty()) {
      return createCallback();
    } else {
      return Mono.just(itemBuffer.poll());
    }
  }

  private Mono<T> createCallback() {
    final Consumer<MonoSink<T>> stringConsumer = tMonoSink -> {
      callables.offer(new MonoSinkHelper<>(tMonoSink));
    };
    return Mono.create(stringConsumer);
  }

  private void emitNextItem(FluxSink<T> stringFluxSink, T a) {
    if (callables.isEmpty()) {
      bufferItem(stringFluxSink, a);
    } else {
      emitToNextSubscribedCaller(stringFluxSink, a);
    }
  }

  private void bufferItem(FluxSink<T> stringFluxSink, T a) {
    stringFluxSink.next(a);
    itemBuffer.offer(a);
  }

  private void emitToNextSubscribedCaller(FluxSink<T> stringFluxSink, T a) {
    MonoSinkHelper<T> nextPersonInLine = callables.poll();
    if (nextPersonInLine.isDisposed()) {
      emitNextItem(stringFluxSink, a);
    } else {
      nextPersonInLine.success(a);
    }
  }


  private void accept(Throwable b) {
    callables.forEach(callable -> callable.error(b));
  }

  private void run() {
    callables.forEach(MonoSinkHelper::success);
    complete = true;
  }

}
