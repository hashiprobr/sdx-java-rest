package br.pro.hashi.sdx.rest.server.tree.mock.endpoint;

import java.util.List;

import br.pro.hashi.sdx.rest.server.RestResource;
import br.pro.hashi.sdx.rest.server.annotation.Body;
import br.pro.hashi.sdx.rest.server.annotation.Part;

public class Signatures extends RestResource {
	public boolean withReturn() {
		return true;
	}

	public void withNothing() {
	}

	public void withVarArgs(int... args) {
	}

	public void withOneItem(int i) {
	}

	public void withOneItemAndVarArgs(int i, double... args) {
	}

	public void withTwoItems(int i, double d) {
	}

	public void withOneItemAndOnePart(int i, @Part("name") Object part) {
	}

	public void withOneItemAndVarBody(int i, @Body Object... body) {
	}

	public void withOneItemAndOneBody(int i, @Body Object body) {
	}

	public void withOnePart(@Part("name") Object part) {
	}

	public void withOnePartAndVarArgs(@Part("name") Object part, int... args) {
	}

	public void withOnePartAndOneItem(@Part("name") Object part, int i) {
	}

	public void withTwoPartsAndOneName(@Part("name") Object part0, @Part("name") String part1) {
	}

	public void withTwoPartsAndTwoNames(@Part("name0") Object part0, @Part("name1") String part1) {
	}

	public void withOnePartAndVarBody(@Part("name") Object part, @Body String... body) {
	}

	public void withOnePartAndOneBody(@Part("name") Object part, @Body String body) {
	}

	public void withVarBody(@Body Object... body) {
	}

	public void withOneBody(@Body Object body) {
	}

	public void withOneBodyAndVarArgs(@Body Object body, int... args) {
	}

	public void withOneBodyAndOneItem(@Body Object body, int i) {
	}

	public void withOneBodyAndOnePart(@Body Object body, @Part("name") String part) {
	}

	public void withOneBodyAndVarBody(@Body Object body0, @Body String... body1) {
	}

	public void withTwoBodies(@Body Object body0, @Body String body1) {
	}

	public boolean withEverythingAndTwoParts(int i, @Part("name0") Object part0, double d, @Part("name1") String part1) {
		return true;
	}

	public boolean withEverythingAndVarArgs(int i, @Body Object body, double... args) {
		return true;
	}

	public boolean withEverythingAndOneBody(int i, @Body Object body, double d) {
		return true;
	}

	public void withOnePartAndBody(@Part("name") @Body Object both) {
	}

	public void with_Underscore() {
	}

	public List<Boolean> withGenericReturn() {
		return List.of(false, true);
	}

	public void withGenericItem(List<Integer> l) {
	}

	public void withGenericPart(@Part("name") List<Object> parts) {
	}

	public void withGenericBody(@Body List<Object> bodies) {
	}

	public void withUncheckedException() throws RuntimeException {
		throw new RuntimeException();
	}

	public void withCheckedException() throws Exception {
		throw new Exception();
	}

	void withoutPublic() {
	}
}
