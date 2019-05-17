package box.star.bin.sh.promise;

public interface FactoryFunction {
  String getName();

  Process exec(String... parameters);
}
