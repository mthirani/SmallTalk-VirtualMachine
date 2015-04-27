package smalltalk.test;

import org.junit.Test;
import smalltalk.vm.exceptions.BlockCannotReturn;

import static org.junit.Assert.assertEquals;

public class TestBlocks extends BaseTest {
	@Test public void testBlockDescriptor() {
		String input =
			"^[99]";
		String expecting = "a BlockDescriptor";
		execAndCheck(input, expecting);
	}

	@Test public void testBlockValueOperator() {
		/*
		0000:  dbg '<string>', 1:6              MainClass>>main[][]
		0007:  block          0                 MainClass>>main[][main-block0]
		0010:  send           0, 'value'        MainClass>>main[][], MainClass>>main-block0[][]
		0000:  push_int       99                MainClass>>main[][], MainClass>>main-block0[][99]
		0005:  dbg '<string>', 1:4              MainClass>>main[][], MainClass>>main-block0[][99]
		0012:  block_return                     MainClass>>main[][99]
		0015:  dbg '<string>', 1:0              MainClass>>main[][99]
		0022:  return
		 */
		String input =
			"^[99] value"; // block return value is the value of the last expression, 99
		String expecting = "99";
		execAndCheck(input, expecting);
	}

	@Test public void testBlockValueWithArgOperator() {
		/*
		0000:  block          0                 MainClass>>main[][main-block0]
		0003:  push_int       99                MainClass>>main[][main-block0, 99]
		0008:  dbg '<string>', 1:10             MainClass>>main[][main-block0, 99]
		0015:  send           1, 'value:'       MainClass>>main[][], MainClass>>main-block0[99][]
		0000:  push_local     0, 0              MainClass>>main[][], MainClass>>main-block0[99][99]
		0005:  dbg '<string>', 1:8              MainClass>>main[][], MainClass>>main-block0[99][99]
		0012:  block_return                     MainClass>>main[][99]
		0020:  dbg '<string>', 1:0              MainClass>>main[][99]
		0027:  return
		 */
		String input =
			"^[:x | x] value: 99";
		String expecting = "99";
		execAndCheck(input, expecting);
	}

	@Test public void testEvalReturnBlock() {
		/*
		0000:  dbg '<string>', 5:0              MainClass>>main[nil][]
		0007:  dbg '<string>', 5:7              MainClass>>main[nil][]
		0014:  push_global    'T'               MainClass>>main[nil][class T]
		0017:  send           0, 'new'          MainClass>>main[nil][], Object>>new[][]
		0000:  dbg 'image.st', 20:22            MainClass>>main[nil][], Object>>new[][]
		0007:  dbg 'image.st', 20:13            MainClass>>main[nil][], Object>>new[][]
		0014:  self                             MainClass>>main[nil][], Object>>new[][class T]
		0015:  send           0, 'basicNew'     MainClass>>main[nil][], Object>>new[][a T]
		0020:  send           0, 'initialize'   MainClass>>main[nil][], Object>>new[][], Object>>initialize[][]
		0000:  self                             MainClass>>main[nil][], Object>>new[][], Object>>initialize[][a T]
		0001:  dbg 'image.st', 29:16            MainClass>>main[nil][], Object>>new[][], Object>>initialize[][a T]
		0008:  return                           MainClass>>main[nil][], Object>>new[][a T]
		0025:  dbg 'image.st', 20:7             MainClass>>main[nil][], Object>>new[][a T]
		0032:  return                           MainClass>>main[nil][a T]
		0022:  store_local    0, 0              MainClass>>main[a T][a T]
		0027:  pop                              MainClass>>main[a T][]
		0028:  dbg '<string>', 6:5              MainClass>>main[a T][]
		0035:  dbg '<string>', 6:3              MainClass>>main[a T][]
		0042:  push_local     0, 0              MainClass>>main[a T][a T]
		0047:  send           0, 'f'            MainClass>>main[a T][], T>>f[nil][]
		0000:  dbg '<string>', 2:11             MainClass>>main[a T][], T>>f[nil][]
		0007:  push_int       1                 MainClass>>main[a T][], T>>f[nil][1]
		0012:  store_local    0, 0              MainClass>>main[a T][], T>>f[1][1]
		0017:  pop                              MainClass>>main[a T][], T>>f[1][]
		0018:  block          0                 MainClass>>main[a T][], T>>f[1][f-block0]
		0021:  dbg '<string>', 2:19             MainClass>>main[a T][], T>>f[1][f-block0]
		0028:  return                           MainClass>>main[a T][f-block0]
		0052:  send           0, 'value'        MainClass>>main[a T][], T>>f-block0[][]
		0000:  dbg '<string>', 2:21             MainClass>>main[a T][], T>>f-block0[][]
		0007:  push_int       5                 MainClass>>main[a T][], T>>f-block0[][5]
		0012:  store_local    1, 0              MainClass>>main[a T][], T>>f-block0[][5]
		0017:  dbg '<string>', 2:27             MainClass>>main[a T][], T>>f-block0[][5]
		0024:  block_return                     MainClass>>main[a T][5]
		0057:  dbg '<string>', 6:0              MainClass>>main[a T][5]
		0064:  return
		 */
		String input =
			"class T [\n" +
			"    f [|x| x := 1. ^[x := 5] ]\n" + // return block that assigns to local x
			"]\n" +
			"|t|\n" +
			"t := T new.\n" +
			"^t f value"; // Send f to t then evaluate the block that comes back.
			              // Even though f has returned, the [x := 5] can access x
		                  // Result is last expr of [x := 5] or 5.
		String expecting = "5";
		execAndCheck(input, expecting);
	}

	@Test public void testRemoteMethodCanSetMyLocal() {
		/*
		0000:  dbg '<string>', 5:7              MainClass>>main[][]
		0007:  dbg '<string>', 5:3              MainClass>>main[][]
		0014:  push_global    'T'               MainClass>>main[][class T]
		0017:  send           0, 'new'          MainClass>>main[][], Object>>new[][]
		0000:  dbg 'image.st', 20:22            MainClass>>main[][], Object>>new[][]
		0007:  dbg 'image.st', 20:13            MainClass>>main[][], Object>>new[][]
		0014:  self                             MainClass>>main[][], Object>>new[][class T]
		0015:  send           0, 'basicNew'     MainClass>>main[][], Object>>new[][a T]
		0020:  send           0, 'initialize'   MainClass>>main[][], Object>>new[][], Object>>initialize[][]
		0000:  self                             MainClass>>main[][], Object>>new[][], Object>>initialize[][a T]
		0001:  dbg 'image.st', 29:16            MainClass>>main[][], Object>>new[][], Object>>initialize[][a T]
		0008:  return                           MainClass>>main[][], Object>>new[][a T]
		0025:  dbg 'image.st', 20:7             MainClass>>main[][], Object>>new[][a T]
		0032:  return                           MainClass>>main[][a T]
		0022:  send           0, 'f'            MainClass>>main[][], T>>f[nil][]
		0000:  self                             MainClass>>main[][], T>>f[nil][a T]
		0001:  block          0                 MainClass>>main[][], T>>f[nil][a T, f-block0]
		0004:  dbg '<string>', 2:16             MainClass>>main[][], T>>f[nil][a T, f-block0]
		0011:  send           1, 'g:'           MainClass>>main[][], T>>f[nil][], T>>g:[f-block0][]
		0000:  dbg '<string>', 3:17             MainClass>>main[][], T>>f[nil][], T>>g:[f-block0][]
		0007:  push_local     0, 0              MainClass>>main[][], T>>f[nil][], T>>g:[f-block0][f-block0]
		0012:  send           0, 'value'        MainClass>>main[][], T>>f[nil][], T>>g:[f-block0][], T>>f-block0[][]
		0000:  dbg '<string>', 2:20             MainClass>>main[][], T>>f[nil][], T>>g:[f-block0][], T>>f-block0[][]
		0007:  push_int       5                 MainClass>>main[][], T>>f[nil][], T>>g:[f-block0][], T>>f-block0[][5]
		0012:  store_local    1, 0              MainClass>>main[][], T>>f[5][], T>>g:[f-block0][], T>>f-block0[][5]
		0017:  dbg '<string>', 2:26             MainClass>>main[][], T>>f[5][], T>>g:[f-block0][], T>>f-block0[][5]
		0024:  block_return                     MainClass>>main[][], T>>f[5][], T>>g:[f-block0][5]
		0017:  dbg '<string>', 3:23             MainClass>>main[][], T>>f[5][], T>>g:[f-block0][5]
		0024:  pop                              MainClass>>main[][], T>>f[5][], T>>g:[f-block0][]
		0025:  self                             MainClass>>main[][], T>>f[5][], T>>g:[f-block0][a T]
		0026:  return                           MainClass>>main[][], T>>f[5][a T]
		0016:  pop                              MainClass>>main[][], T>>f[5][]
		0017:  push_local     0, 0              MainClass>>main[][], T>>f[5][5]
		0022:  dbg '<string>', 2:29             MainClass>>main[][], T>>f[5][5]
		0029:  return                           MainClass>>main[][5]
		0027:  dbg '<string>', 5:0              MainClass>>main[][5]
		0034:  return
		 */
		String input =
		"class T [\n" +
		"    f [|x| self g: [x := 5]. ^x]\n" +  // pass block to g that assigns value to local x
		"    g: blk [ blk value ]\n"+           // eval block [x:=5] then return to f, which returns x
		"]\n" +
		"^T new f"; // make a T then call f
		String expecting = "5";
		execAndCheck(input, expecting);
	}

	@Test public void testRemoteMethodCanSetMyArg() {
		/*
		0000:  dbg '<string>', 5:3              MainClass>>main[][]
		0007:  push_global    'T'               MainClass>>main[][class T]
		0010:  send           0, 'new'          MainClass>>main[][], Object>>new[][]
		0000:  dbg 'image.st', 20:22            MainClass>>main[][], Object>>new[][]
		0007:  dbg 'image.st', 20:13            MainClass>>main[][], Object>>new[][]
		0014:  self                             MainClass>>main[][], Object>>new[][class T]
		0015:  send           0, 'basicNew'     MainClass>>main[][], Object>>new[][a T]
		0020:  send           0, 'initialize'   MainClass>>main[][], Object>>new[][], Object>>initialize[][]
		0000:  self                             MainClass>>main[][], Object>>new[][], Object>>initialize[][a T]
		0001:  dbg 'image.st', 29:16            MainClass>>main[][], Object>>new[][], Object>>initialize[][a T]
		0008:  return                           MainClass>>main[][], Object>>new[][a T]
		0025:  dbg 'image.st', 20:7             MainClass>>main[][], Object>>new[][a T]
		0032:  return                           MainClass>>main[][a T]
		0015:  push_int       1                 MainClass>>main[][a T, 1]
		0020:  dbg '<string>', 5:7              MainClass>>main[][a T, 1]
		0027:  send           1, 'f:'           MainClass>>main[][], T>>f:[1][]
		0000:  self                             MainClass>>main[][], T>>f:[1][a T]
		0001:  block          0                 MainClass>>main[][], T>>f:[1][a T, f:-block0]
		0004:  dbg '<string>', 2:15             MainClass>>main[][], T>>f:[1][a T, f:-block0]
		0011:  send           1, 'g:'           MainClass>>main[][], T>>f:[1][], T>>g:[f:-block0][]
		0000:  dbg '<string>', 3:17             MainClass>>main[][], T>>f:[1][], T>>g:[f:-block0][]
		0007:  push_local     0, 0              MainClass>>main[][], T>>f:[1][], T>>g:[f:-block0][f:-block0]
		0012:  send           0, 'value'        MainClass>>main[][], T>>f:[1][], T>>g:[f:-block0][], T>>f:-block0[][]
		0000:  dbg '<string>', 2:19             MainClass>>main[][], T>>f:[1][], T>>g:[f:-block0][], T>>f:-block0[][]
		0007:  push_int       5                 MainClass>>main[][], T>>f:[1][], T>>g:[f:-block0][], T>>f:-block0[][5]
		0012:  store_local    1, 0              MainClass>>main[][], T>>f:[5][], T>>g:[f:-block0][], T>>f:-block0[][5]
		0017:  dbg '<string>', 2:25             MainClass>>main[][], T>>f:[5][], T>>g:[f:-block0][], T>>f:-block0[][5]
		0024:  block_return                     MainClass>>main[][], T>>f:[5][], T>>g:[f:-block0][5]
		0017:  dbg '<string>', 3:23             MainClass>>main[][], T>>f:[5][], T>>g:[f:-block0][5]
		0024:  pop                              MainClass>>main[][], T>>f:[5][], T>>g:[f:-block0][]
		0025:  self                             MainClass>>main[][], T>>f:[5][], T>>g:[f:-block0][a T]
		0026:  return                           MainClass>>main[][], T>>f:[5][a T]
		0016:  pop                              MainClass>>main[][], T>>f:[5][]
		0017:  push_local     0, 0              MainClass>>main[][], T>>f:[5][5]
		0022:  dbg '<string>', 2:28             MainClass>>main[][], T>>f:[5][5]
		0029:  return                           MainClass>>main[][5]
		0032:  dbg '<string>', 5:0              MainClass>>main[][5]
		0039:  return
		 */
		String input =
		"class T [\n" +
		"    f: x [self g: [x := 5]. ^x]\n" + // pass block to g that assigns value to arg x
		"    g: blk [ blk value ]\n"+         // eval block [x:=5] then return to f, which returns x
		"]\n" +
		"^T new f: 1"; // make a T then call f
		String expecting = "5";
		execAndCheck(input, expecting);
	}

	@Test public void testRemoteMethodCanSetMyField() {
		/*
		0000:  dbg '<string>', 9:7              MainClass>>main[][]
		0007:  dbg '<string>', 9:3              MainClass>>main[][]
		0014:  push_global    'T'               MainClass>>main[][class T]
		0017:  send           0, 'new'          MainClass>>main[][], Object>>new[][]
		0000:  dbg 'image.st', 20:22            MainClass>>main[][], Object>>new[][]
		0007:  dbg 'image.st', 20:13            MainClass>>main[][], Object>>new[][]
		0014:  self                             MainClass>>main[][], Object>>new[][class T]
		0015:  send           0, 'basicNew'     MainClass>>main[][], Object>>new[][a T]
		0020:  send           0, 'initialize'   MainClass>>main[][], Object>>new[][], T>>initialize[][]
		0000:  dbg '<string>', 3:16             MainClass>>main[][], Object>>new[][], T>>initialize[][]
		0007:  push_int       1                 MainClass>>main[][], Object>>new[][], T>>initialize[][1]
		0012:  store_field    0                 MainClass>>main[][], Object>>new[][], T>>initialize[][1]
		0015:  dbg '<string>', 3:22             MainClass>>main[][], Object>>new[][], T>>initialize[][1]
		0022:  pop                              MainClass>>main[][], Object>>new[][], T>>initialize[][]
		0023:  self                             MainClass>>main[][], Object>>new[][], T>>initialize[][a T]
		0024:  return                           MainClass>>main[][], Object>>new[][a T]
		0025:  dbg 'image.st', 20:7             MainClass>>main[][], Object>>new[][a T]
		0032:  return                           MainClass>>main[][a T]
		0022:  send           0, 'f'            MainClass>>main[][], T>>f[][]
		0000:  dbg '<string>', 4:10             MainClass>>main[][], T>>f[][]
		0007:  push_global    'U'               MainClass>>main[][], T>>f[][class U]
		0010:  send           0, 'new'          MainClass>>main[][], T>>f[][], Object>>new[][]
		0000:  dbg 'image.st', 20:22            MainClass>>main[][], T>>f[][], Object>>new[][]
		0007:  dbg 'image.st', 20:13            MainClass>>main[][], T>>f[][], Object>>new[][]
		0014:  self                             MainClass>>main[][], T>>f[][], Object>>new[][class U]
		0015:  send           0, 'basicNew'     MainClass>>main[][], T>>f[][], Object>>new[][a U]
		0020:  send           0, 'initialize'   MainClass>>main[][], T>>f[][], Object>>new[][], Object>>initialize[][]
		0000:  self                             MainClass>>main[][], T>>f[][], Object>>new[][], Object>>initialize[][a U]
		0001:  dbg 'image.st', 29:16            MainClass>>main[][], T>>f[][], Object>>new[][], Object>>initialize[][a U]
		0008:  return                           MainClass>>main[][], T>>f[][], Object>>new[][a U]
		0025:  dbg 'image.st', 20:7             MainClass>>main[][], T>>f[][], Object>>new[][a U]
		0032:  return                           MainClass>>main[][], T>>f[][a U]
		0015:  block          0                 MainClass>>main[][], T>>f[][a U, f-block0]
		0018:  dbg '<string>', 4:15             MainClass>>main[][], T>>f[][a U, f-block0]
		0025:  send           1, 'g:'           MainClass>>main[][], T>>f[][], U>>g:[f-block0][]
		0000:  dbg '<string>', 7:17             MainClass>>main[][], T>>f[][], U>>g:[f-block0][]
		0007:  push_local     0, 0              MainClass>>main[][], T>>f[][], U>>g:[f-block0][f-block0]
		0012:  send           0, 'value'        MainClass>>main[][], T>>f[][], U>>g:[f-block0][], T>>f-block0[][]
		0000:  dbg '<string>', 4:19             MainClass>>main[][], T>>f[][], U>>g:[f-block0][], T>>f-block0[][]
		0007:  push_int       5                 MainClass>>main[][], T>>f[][], U>>g:[f-block0][], T>>f-block0[][5]
		0012:  store_field    0                 MainClass>>main[][], T>>f[][], U>>g:[f-block0][], T>>f-block0[][5]
		0015:  dbg '<string>', 4:25             MainClass>>main[][], T>>f[][], U>>g:[f-block0][], T>>f-block0[][5]
		0022:  block_return                     MainClass>>main[][], T>>f[][], U>>g:[f-block0][5]
		0017:  dbg '<string>', 7:23             MainClass>>main[][], T>>f[][], U>>g:[f-block0][5]
		0024:  pop                              MainClass>>main[][], T>>f[][], U>>g:[f-block0][]
		0025:  self                             MainClass>>main[][], T>>f[][], U>>g:[f-block0][a U]
		0026:  return                           MainClass>>main[][], T>>f[][a U]
		0030:  pop                              MainClass>>main[][], T>>f[][]
		0031:  push_field     0                 MainClass>>main[][], T>>f[][5]
		0034:  dbg '<string>', 4:28             MainClass>>main[][], T>>f[][5]
		0041:  return                           MainClass>>main[][5]
		0027:  dbg '<string>', 9:0              MainClass>>main[][5]
		0034:  return
		 */
		String input =
		"class T [\n" +
		"    | x |\n" +
		"    initialize [x := 1]\n" +
		"    f [(U new) g: [x := 5]. ^x]\n" + // pass block to g that assigns value to arg x
		"]\n" +
		"class U [\n" +
		"    g: blk [ blk value ]\n"+         // eval block [x:=5] then return to f, which returns x
		"]\n" +
		"^T new f"; // make a T then call f
		String expecting = "5";
		execAndCheck(input, expecting);
	}

	@Test public void testSendBlockBackToSameMethodAndSetLocal() {
		/*
		0000:  dbg '<string>', 10:3             MainClass>>main[][]
		0007:  push_global    'T'               MainClass>>main[][class T]
		0010:  send           0, 'new'          MainClass>>main[][], Object>>new[][]
		0000:  dbg 'image.st', 20:22            MainClass>>main[][], Object>>new[][]
		0007:  dbg 'image.st', 20:13            MainClass>>main[][], Object>>new[][]
		0014:  self                             MainClass>>main[][], Object>>new[][class T]
		0015:  send           0, 'basicNew'     MainClass>>main[][], Object>>new[][a T]
		0020:  send           0, 'initialize'   MainClass>>main[][], Object>>new[][], Object>>initialize[][]
		0000:  self                             MainClass>>main[][], Object>>new[][], Object>>initialize[][a T]
		0001:  dbg 'image.st', 29:16            MainClass>>main[][], Object>>new[][], Object>>initialize[][a T]
		0008:  return                           MainClass>>main[][], Object>>new[][a T]
		0025:  dbg 'image.st', 20:7             MainClass>>main[][], Object>>new[][a T]
		0032:  return                           MainClass>>main[][a T]
		0015:  nil                              MainClass>>main[][a T, nil]
		0016:  push_int       1                 MainClass>>main[][a T, nil, 1]
		0021:  dbg '<string>', 10:7             MainClass>>main[][a T, nil, 1]
		0028:  send           2, 'f:pass:'      MainClass>>main[][], T>>f:pass:[nil, 1, nil][]
		0000:  dbg '<string>', 4:8              MainClass>>main[][], T>>f:pass:[nil, 1, nil][]
		0007:  push_local     0, 1              MainClass>>main[][], T>>f:pass:[nil, 1, nil][1]
		0012:  push_int       1                 MainClass>>main[][], T>>f:pass:[nil, 1, nil][1, 1]
		0017:  send           1, '='            MainClass>>main[][], T>>f:pass:[nil, 1, nil][true]
		0022:  block          0                 MainClass>>main[][], T>>f:pass:[nil, 1, nil][true, f:pass:-block0]
		0025:  block          2                 MainClass>>main[][], T>>f:pass:[nil, 1, nil][true, f:pass:-block0, f:pass:-block2]
		0028:  dbg '<string>', 4:11             MainClass>>main[][], T>>f:pass:[nil, 1, nil][true, f:pass:-block0, f:pass:-block2]
		0035:  send           2, 'ifTrue:ifFalse:'MainClass>>main[][], T>>f:pass:[nil, 1, nil][], T>>f:pass:-block0[][]
		0000:  self                             MainClass>>main[][], T>>f:pass:[nil, 1, nil][], T>>f:pass:-block0[][a T]
		0001:  block          1                 MainClass>>main[][], T>>f:pass:[nil, 1, nil][], T>>f:pass:-block0[][a T, f:pass:-block1]
		0004:  dbg '<string>', 4:25             MainClass>>main[][], T>>f:pass:[nil, 1, nil][], T>>f:pass:-block0[][a T, f:pass:-block1]
		0011:  send           1, 'g:'           MainClass>>main[][], T>>f:pass:[nil, 1, nil][], T>>f:pass:-block0[][], T>>g:[f:pass:-block1][]
		0000:  self                             MainClass>>main[][], T>>f:pass:[nil, 1, nil][], T>>f:pass:-block0[][], T>>g:[f:pass:-block1][a T]
		0001:  push_local     0, 0              MainClass>>main[][], T>>f:pass:[nil, 1, nil][], T>>f:pass:-block0[][], T>>g:[f:pass:-block1][a T, f:pass:-block1]
		0006:  push_int       2                 MainClass>>main[][], T>>f:pass:[nil, 1, nil][], T>>f:pass:-block0[][], T>>g:[f:pass:-block1][a T, f:pass:-block1, 2]
		0011:  dbg '<string>', 8:18             MainClass>>main[][], T>>f:pass:[nil, 1, nil][], T>>f:pass:-block0[][], T>>g:[f:pass:-block1][a T, f:pass:-block1, 2]
		0018:  send           2, 'f:pass:'      MainClass>>main[][], T>>f:pass:[nil, 1, nil][], T>>f:pass:-block0[][], T>>g:[f:pass:-block1][], T>>f:pass:[f:pass:-block1, 2, nil][]
		0000:  dbg '<string>', 4:8              MainClass>>main[][], T>>f:pass:[nil, 1, nil][], T>>f:pass:-block0[][], T>>g:[f:pass:-block1][], T>>f:pass:[f:pass:-block1, 2, nil][]
		0007:  push_local     0, 1              MainClass>>main[][], T>>f:pass:[nil, 1, nil][], T>>f:pass:-block0[][], T>>g:[f:pass:-block1][], T>>f:pass:[f:pass:-block1, 2, nil][2]
		0012:  push_int       1                 MainClass>>main[][], T>>f:pass:[nil, 1, nil][], T>>f:pass:-block0[][], T>>g:[f:pass:-block1][], T>>f:pass:[f:pass:-block1, 2, nil][2, 1]
		0017:  send           1, '='            MainClass>>main[][], T>>f:pass:[nil, 1, nil][], T>>f:pass:-block0[][], T>>g:[f:pass:-block1][], T>>f:pass:[f:pass:-block1, 2, nil][false]
		0022:  block          0                 MainClass>>main[][], T>>f:pass:[nil, 1, nil][], T>>f:pass:-block0[][], T>>g:[f:pass:-block1][], T>>f:pass:[f:pass:-block1, 2, nil][false, f:pass:-block0]
		0025:  block          2                 MainClass>>main[][], T>>f:pass:[nil, 1, nil][], T>>f:pass:-block0[][], T>>g:[f:pass:-block1][], T>>f:pass:[f:pass:-block1, 2, nil][false, f:pass:-block0, f:pass:-block2]
		0028:  dbg '<string>', 4:11             MainClass>>main[][], T>>f:pass:[nil, 1, nil][], T>>f:pass:-block0[][], T>>g:[f:pass:-block1][], T>>f:pass:[f:pass:-block1, 2, nil][false, f:pass:-block0, f:pass:-block2]
		0035:  send           2, 'ifTrue:ifFalse:'MainClass>>main[][], T>>f:pass:[nil, 1, nil][], T>>f:pass:-block0[][], T>>g:[f:pass:-block1][], T>>f:pass:[f:pass:-block1, 2, nil][], T>>f:pass:-block2[][]
		0000:  dbg '<string>', 5:24             MainClass>>main[][], T>>f:pass:[nil, 1, nil][], T>>f:pass:-block0[][], T>>g:[f:pass:-block1][], T>>f:pass:[f:pass:-block1, 2, nil][], T>>f:pass:-block2[][]
		0007:  push_local     1, 0              MainClass>>main[][], T>>f:pass:[nil, 1, nil][], T>>f:pass:-block0[][], T>>g:[f:pass:-block1][], T>>f:pass:[f:pass:-block1, 2, nil][], T>>f:pass:-block2[][f:pass:-block1]
		0012:  send           0, 'value'        MainClass>>main[][], T>>f:pass:[nil, 1, nil][], T>>f:pass:-block0[][], T>>g:[f:pass:-block1][], T>>f:pass:[f:pass:-block1, 2, nil][], T>>f:pass:-block2[][], T>>f:pass:-block1[][]
		0000:  dbg '<string>', 4:29             MainClass>>main[][], T>>f:pass:[nil, 1, nil][], T>>f:pass:-block0[][], T>>g:[f:pass:-block1][], T>>f:pass:[f:pass:-block1, 2, nil][], T>>f:pass:-block2[][], T>>f:pass:-block1[][]
		0007:  push_int       5                 MainClass>>main[][], T>>f:pass:[nil, 1, nil][], T>>f:pass:-block0[][], T>>g:[f:pass:-block1][], T>>f:pass:[f:pass:-block1, 2, nil][], T>>f:pass:-block2[][], T>>f:pass:-block1[][5]
		0012:  store_local    2, 2              MainClass>>main[][], T>>f:pass:[nil, 1, 5][], T>>f:pass:-block0[][], T>>g:[f:pass:-block1][], T>>f:pass:[f:pass:-block1, 2, nil][], T>>f:pass:-block2[][], T>>f:pass:-block1[][5]
		0017:  dbg '<string>', 4:33             MainClass>>main[][], T>>f:pass:[nil, 1, 5][], T>>f:pass:-block0[][], T>>g:[f:pass:-block1][], T>>f:pass:[f:pass:-block1, 2, nil][], T>>f:pass:-block2[][], T>>f:pass:-block1[][5]
		0024:  block_return                     MainClass>>main[][], T>>f:pass:[nil, 1, 5][], T>>f:pass:-block0[][], T>>g:[f:pass:-block1][], T>>f:pass:[f:pass:-block1, 2, nil][], T>>f:pass:-block2[][5]
		0017:  dbg '<string>', 5:29             MainClass>>main[][], T>>f:pass:[nil, 1, 5][], T>>f:pass:-block0[][], T>>g:[f:pass:-block1][], T>>f:pass:[f:pass:-block1, 2, nil][], T>>f:pass:-block2[][5]
		0024:  block_return                     MainClass>>main[][], T>>f:pass:[nil, 1, 5][], T>>f:pass:-block0[][], T>>g:[f:pass:-block1][], T>>f:pass:[f:pass:-block1, 2, nil][5]
		0040:  pop                              MainClass>>main[][], T>>f:pass:[nil, 1, 5][], T>>f:pass:-block0[][], T>>g:[f:pass:-block1][], T>>f:pass:[f:pass:-block1, 2, nil][]
		0041:  push_local     0, 2              MainClass>>main[][], T>>f:pass:[nil, 1, 5][], T>>f:pass:-block0[][], T>>g:[f:pass:-block1][], T>>f:pass:[f:pass:-block1, 2, nil][nil]
		0046:  dbg '<string>', 6:7              MainClass>>main[][], T>>f:pass:[nil, 1, 5][], T>>f:pass:-block0[][], T>>g:[f:pass:-block1][], T>>f:pass:[f:pass:-block1, 2, nil][nil]
		0053:  return                           MainClass>>main[][], T>>f:pass:[nil, 1, 5][], T>>f:pass:-block0[][], T>>g:[f:pass:-block1][nil]
		0023:  dbg '<string>', 8:32             MainClass>>main[][], T>>f:pass:[nil, 1, 5][], T>>f:pass:-block0[][], T>>g:[f:pass:-block1][nil]
		0030:  pop                              MainClass>>main[][], T>>f:pass:[nil, 1, 5][], T>>f:pass:-block0[][], T>>g:[f:pass:-block1][]
		0031:  self                             MainClass>>main[][], T>>f:pass:[nil, 1, 5][], T>>f:pass:-block0[][], T>>g:[f:pass:-block1][a T]
		0032:  return                           MainClass>>main[][], T>>f:pass:[nil, 1, 5][], T>>f:pass:-block0[][a T]
		0016:  dbg '<string>', 4:34             MainClass>>main[][], T>>f:pass:[nil, 1, 5][], T>>f:pass:-block0[][a T]
		0023:  block_return                     MainClass>>main[][], T>>f:pass:[nil, 1, 5][a T]
		0040:  pop                              MainClass>>main[][], T>>f:pass:[nil, 1, 5][]
		0041:  push_local     0, 2              MainClass>>main[][], T>>f:pass:[nil, 1, 5][5]
		0046:  dbg '<string>', 6:7              MainClass>>main[][], T>>f:pass:[nil, 1, 5][5]
		0053:  return                           MainClass>>main[][5]
		0033:  dbg '<string>', 10:0             MainClass>>main[][5]
		0040:  return
		 */
		String input =
		"class T [\n" +
		"    f: blk pass: p [\n" +
		"       |x|\n" +
		"       p=1 ifTrue: [self g: [x:=5]]\n" + // pass 1: send to g, which sends back to us
		"           ifFalse:[blk value].\n" +     // pass 2: eval blk [x:=5] passed back from g
		"       ^x\n" +                           // should be 5 not nil
		"    ]\n" +
		"    g: blk [ self f: blk pass: 2]\n"+   // send blk back to f for eval
		"]\n" +
		"^T new f: nil pass: 1"; // make a T then call a:b:
		String expecting = "5";
		execAndCheck(input, expecting);
	}

	@Test public void testRemoteReturn() {
		/*
		0000:  dbg '<string>', 6:0              MainClass>>main[nil][]
		0007:  dbg '<string>', 6:7              MainClass>>main[nil][]
		0014:  push_global    'T'               MainClass>>main[nil][class T]
		0017:  send           0, 'new'          MainClass>>main[nil][], Object>>new[][]
		0000:  dbg 'image.st', 20:22            MainClass>>main[nil][], Object>>new[][]
		0007:  dbg 'image.st', 20:13            MainClass>>main[nil][], Object>>new[][]
		0014:  self                             MainClass>>main[nil][], Object>>new[][class T]
		0015:  send           0, 'basicNew'     MainClass>>main[nil][], Object>>new[][a T]
		0020:  send           0, 'initialize'   MainClass>>main[nil][], Object>>new[][], Object>>initialize[][]
		0000:  self                             MainClass>>main[nil][], Object>>new[][], Object>>initialize[][a T]
		0001:  dbg 'image.st', 29:16            MainClass>>main[nil][], Object>>new[][], Object>>initialize[][a T]
		0008:  return                           MainClass>>main[nil][], Object>>new[][a T]
		0025:  dbg 'image.st', 20:7             MainClass>>main[nil][], Object>>new[][a T]
		0032:  return                           MainClass>>main[nil][a T]
		0022:  store_local    0, 0              MainClass>>main[a T][a T]
		0027:  pop                              MainClass>>main[a T][]
		0028:  dbg '<string>', 7:3              MainClass>>main[a T][]
		0035:  push_local     0, 0              MainClass>>main[a T][a T]
		0040:  send           0, 'f'            MainClass>>main[a T][], T>>f[][]
		0000:  self                             MainClass>>main[a T][], T>>f[][a T]
		0001:  block          0                 MainClass>>main[a T][], T>>f[][a T, f-block0]
		0004:  dbg '<string>', 2:13             MainClass>>main[a T][], T>>f[][a T, f-block0]
		0011:  send           1, 'g:'           MainClass>>main[a T][], T>>f[][], T>>g:[f-block0][]
		0000:  dbg '<string>', 3:17             MainClass>>main[a T][], T>>f[][], T>>g:[f-block0][]
		0007:  push_local     0, 0              MainClass>>main[a T][], T>>f[][], T>>g:[f-block0][f-block0]
		0012:  send           0, 'value'        MainClass>>main[a T][], T>>f[][], T>>g:[f-block0][], T>>f-block0[][]
		0000:  push_int       99                MainClass>>main[a T][], T>>f[][], T>>g:[f-block0][], T>>f-block0[][99]
		0005:  dbg '<string>', 2:17             MainClass>>main[a T][], T>>f[][], T>>g:[f-block0][], T>>f-block0[][99]
		0012:  return                           MainClass>>main[a T][99]
		0045:  dbg '<string>', 7:0              MainClass>>main[a T][99]
		0052:  return
		 */
		String input =
			"class T [\n" +
			"    f [ self g: [^99]. ^1]\n" + // send a block to g that returns 99 from f. the ^1 is dead code
			"    g: blk [ blk value ]\n" +   // eval block that forces f to return 99.
											 // VM unrolls method invocation stack to caller of f upon evaluating [^99]
			"]\n" +
			"|t|\n" +
			"t := T new.\n" +
			"^t f"; // Send f to t
		String expecting = "99";
		execAndCheck(input, expecting);
	}

	@Test public void testRemoteReturnFromNestedBlock() {
		/*
		0000:  dbg '<string>', 6:0              MainClass>>main[nil][]
		0007:  dbg '<string>', 6:7              MainClass>>main[nil][]
		0014:  push_global    'T'               MainClass>>main[nil][class T]
		0017:  send           0, 'new'          MainClass>>main[nil][], Object>>new[][]
		0000:  dbg 'image.st', 20:22            MainClass>>main[nil][], Object>>new[][]
		0007:  dbg 'image.st', 20:13            MainClass>>main[nil][], Object>>new[][]
		0014:  self                             MainClass>>main[nil][], Object>>new[][class T]
		0015:  send           0, 'basicNew'     MainClass>>main[nil][], Object>>new[][a T]
		0020:  send           0, 'initialize'   MainClass>>main[nil][], Object>>new[][], Object>>initialize[][]
		0000:  self                             MainClass>>main[nil][], Object>>new[][], Object>>initialize[][a T]
		0001:  dbg 'image.st', 29:16            MainClass>>main[nil][], Object>>new[][], Object>>initialize[][a T]
		0008:  return                           MainClass>>main[nil][], Object>>new[][a T]
		0025:  dbg 'image.st', 20:7             MainClass>>main[nil][], Object>>new[][a T]
		0032:  return                           MainClass>>main[nil][a T]
		0022:  store_local    0, 0              MainClass>>main[a T][a T]
		0027:  pop                              MainClass>>main[a T][]
		0028:  dbg '<string>', 7:3              MainClass>>main[a T][]
		0035:  push_local     0, 0              MainClass>>main[a T][a T]
		0040:  send           0, 'f'            MainClass>>main[a T][], T>>f[][]
		0000:  dbg '<string>', 2:24             MainClass>>main[a T][], T>>f[][]
		0007:  block          0                 MainClass>>main[a T][], T>>f[][f-block0]
		0010:  send           0, 'value'        MainClass>>main[a T][], T>>f[][], T>>f-block0[][]
		0000:  self                             MainClass>>main[a T][], T>>f[][], T>>f-block0[][a T]
		0001:  block          1                 MainClass>>main[a T][], T>>f[][], T>>f-block0[][a T, f-block1]
		0004:  dbg '<string>', 2:14             MainClass>>main[a T][], T>>f[][], T>>f-block0[][a T, f-block1]
		0011:  send           1, 'g:'           MainClass>>main[a T][], T>>f[][], T>>f-block0[][], T>>g:[f-block1][]
		0000:  dbg '<string>', 3:17             MainClass>>main[a T][], T>>f[][], T>>f-block0[][], T>>g:[f-block1][]
		0007:  push_local     0, 0              MainClass>>main[a T][], T>>f[][], T>>f-block0[][], T>>g:[f-block1][f-block1]
		0012:  send           0, 'value'        MainClass>>main[a T][], T>>f[][], T>>f-block0[][], T>>g:[f-block1][], T>>f-block1[][]
		0000:  push_int       99                MainClass>>main[a T][], T>>f[][], T>>f-block0[][], T>>g:[f-block1][], T>>f-block1[][99]
		0005:  dbg '<string>', 2:18             MainClass>>main[a T][], T>>f[][], T>>f-block0[][], T>>g:[f-block1][], T>>f-block1[][99]
		0012:  return                           MainClass>>main[a T][99]
		0045:  dbg '<string>', 7:0              MainClass>>main[a T][99]
		0052:  return
		 */
		String input =
			"class T [\n" +
			"    f [ [self g: [^99]] value. ^1]\n" + // eval code that sends a block to g that returns 99 from f. the ^1 is dead code
			"    g: blk [ blk value ]\n" +   // eval block that forces f to return 99.
											 // VM unrolls method invocation stack to caller of f upon evaluating [^99]
			"]\n" +
			"|t|\n" +
			"t := T new.\n" +
			"^t f"; // Send f to t
		String expecting = "99";
		execAndCheck(input, expecting);
	}

	@Test public void testAttemptDoubleReturn() {
		/*
		 */
		String input =
		"class T [\n" +
		"    f [^[^99]]\n" + // return block that returns 99
		"]\n" +
		"|t|\n" +
		"t := T new.\n" +
		"(t f) value"; // Send f to t then evaluate block that comes back, [^99],
		// which tries to return from its surrounding method, f, again.
		String expecting =
		"BlockCannotReturn: T>>f-block0 can't trigger return again from method T>>f\n" +
		"    at                                    f>>f-block0[][](<string>:2:9)       executing 0012:  return           \n" +
		"    at                             MainClass>>main[a T][](<string>:6:3)       executing 0052:  send           0, 'value'\n";
		String result = "";
		try {
			execAndCheck(input, expecting);
		}
		catch (BlockCannotReturn bcr) {
			result = bcr.toString();
		}
		assertEquals(expecting, result);
	}

	@Test public void testAttemptDoubleReturn2() {
		/*
		0000:  dbg '<string>', 6:0              MainClass>>main[nil][]
		0007:  dbg '<string>', 6:7              MainClass>>main[nil][]
		0014:  push_global    'T'               MainClass>>main[nil][class T]
		0017:  send           0, 'new'          MainClass>>main[nil][], Object>>new[][]
		0000:  dbg 'image.st', 20:22            MainClass>>main[nil][], Object>>new[][]
		0007:  dbg 'image.st', 20:13            MainClass>>main[nil][], Object>>new[][]
		0014:  self                             MainClass>>main[nil][], Object>>new[][class T]
		0015:  send           0, 'basicNew'     MainClass>>main[nil][], Object>>new[][a T]
		0020:  send           0, 'initialize'   MainClass>>main[nil][], Object>>new[][], Object>>initialize[][]
		0000:  self                             MainClass>>main[nil][], Object>>new[][], Object>>initialize[][a T]
		0001:  dbg 'image.st', 29:16            MainClass>>main[nil][], Object>>new[][], Object>>initialize[][a T]
		0008:  return                           MainClass>>main[nil][], Object>>new[][a T]
		0025:  dbg 'image.st', 20:7             MainClass>>main[nil][], Object>>new[][a T]
		0032:  return                           MainClass>>main[nil][a T]
		0022:  store_local    0, 0              MainClass>>main[a T][a T]
		0027:  pop                              MainClass>>main[a T][]
		0028:  dbg '<string>', 7:5              MainClass>>main[a T][]
		0035:  dbg '<string>', 7:3              MainClass>>main[a T][]
		0042:  push_local     0, 0              MainClass>>main[a T][a T]
		0047:  send           0, 'f'            MainClass>>main[a T][], T>>f[][]
		0000:  self                             MainClass>>main[a T][], T>>f[][a T]
		0001:  block          0                 MainClass>>main[a T][], T>>f[][a T, f-block0]
		0004:  dbg '<string>', 2:13             MainClass>>main[a T][], T>>f[][a T, f-block0]
		0011:  send           1, 'g:'           MainClass>>main[a T][], T>>f[][], T>>g:[f-block0][]
		0000:  dbg '<string>', 3:17             MainClass>>main[a T][], T>>f[][], T>>g:[f-block0][]
		0007:  push_local     0, 0              MainClass>>main[a T][], T>>f[][], T>>g:[f-block0][f-block0]
		0012:  send           0, 'value'        MainClass>>main[a T][], T>>f[][], T>>g:[f-block0][], T>>f-block0[][]
		0000:  block          1                 MainClass>>main[a T][], T>>f[][], T>>g:[f-block0][], T>>f-block0[][f-block1]
		0003:  dbg '<string>', 2:17             MainClass>>main[a T][], T>>f[][], T>>g:[f-block0][], T>>f-block0[][f-block1]
		0010:  return                           MainClass>>main[a T][f-block1]
		0052:  send           0, 'value'        MainClass>>main[a T][], T>>f-block1[][]
		0000:  push_int       99                MainClass>>main[a T][], T>>f-block1[][99]
		0005:  dbg '<string>', 2:19             MainClass>>main[a T][], T>>f-block1[][99]
		0012:  return
		 */
		String input =
		"class T [\n" +
		"    f [ self g: [^[^99]]. ^1]\n" + // send a block that returns a block that returns 99 from f to g. the ^1 is dead code
		"    g: blk [ blk value ]\n" +      // eval block that forces f to return 99.
		// VM unrolls method invocation stack to caller of f upon evaluating [^99]
		"]\n" +
		"|t|\n" +
		"t := T new.\n" +
		"t f value"; // Send f to t then evaluate block that comes back, [^99],
		// which tries to return from its surrounding method, f, again.
		String expecting =
		"BlockCannotReturn: T>>f-block1 can't trigger return again from method T>>f\n" +
		"    at                             f-block0>>f-block1[][](<string>:2:19)      executing 0012:  return           \n" +
		"    at                             MainClass>>main[a T][](<string>:7:3)       executing 0052:  send           0, 'value'\n";
		String result = "";
		try {
			execAndCheck(input, expecting);
		}
		catch (BlockCannotReturn bcr) {
			result = bcr.toString();
		}
		assertEquals(expecting, result);
	}

	@Test public void returnFromNestedCallViaBlock() {
		/*
		0000:  dbg '<string>', 12:10            MainClass>>main[][]
		0007:  dbg '<string>', 12:6             MainClass>>main[][]
		0014:  push_global    'Test'            MainClass>>main[][class Test]
		0017:  send           0, 'new'          MainClass>>main[][], Object>>new[][]
		0000:  dbg 'image.st', 20:22            MainClass>>main[][], Object>>new[][]
		0007:  dbg 'image.st', 20:13            MainClass>>main[][], Object>>new[][]
		0014:  self                             MainClass>>main[][], Object>>new[][class Test]
		0015:  send           0, 'basicNew'     MainClass>>main[][], Object>>new[][a Test]
		0020:  send           0, 'initialize'   MainClass>>main[][], Object>>new[][], Object>>initialize[][]
		0000:  self                             MainClass>>main[][], Object>>new[][], Object>>initialize[][a Test]
		0001:  dbg 'image.st', 29:16            MainClass>>main[][], Object>>new[][], Object>>initialize[][a Test]
		0008:  return                           MainClass>>main[][], Object>>new[][a Test]
		0025:  dbg 'image.st', 20:7             MainClass>>main[][], Object>>new[][a Test]
		0032:  return                           MainClass>>main[][a Test]
		0022:  send           0, 'test'         MainClass>>main[][], Test>>test[][]
		0000:  self                             MainClass>>main[][], Test>>test[][a Test]
		0001:  block          0                 MainClass>>main[][], Test>>test[][a Test, test-block0]
		0004:  dbg '<string>', 3:9              MainClass>>main[][], Test>>test[][a Test, test-block0]
		0011:  send           1, 'foo:'         MainClass>>main[][], Test>>test[][], Test>>foo:[test-block0][]
		0000:  self                             MainClass>>main[][], Test>>test[][], Test>>foo:[test-block0][a Test]
		0001:  push_local     0, 0              MainClass>>main[][], Test>>test[][], Test>>foo:[test-block0][a Test, test-block0]
		0006:  dbg '<string>', 6:9              MainClass>>main[][], Test>>test[][], Test>>foo:[test-block0][a Test, test-block0]
		0013:  send           1, 'bar:'         MainClass>>main[][], Test>>test[][], Test>>foo:[test-block0][], Test>>bar:[test-block0][]
		0000:  dbg '<string>', 9:8              MainClass>>main[][], Test>>test[][], Test>>foo:[test-block0][], Test>>bar:[test-block0][]
		0007:  push_local     0, 0              MainClass>>main[][], Test>>test[][], Test>>foo:[test-block0][], Test>>bar:[test-block0][test-block0]
		0012:  send           0, 'value'        MainClass>>main[][], Test>>test[][], Test>>foo:[test-block0][], Test>>bar:[test-block0][], Test>>test-block0[][]
		0000:  push_int       99                MainClass>>main[][], Test>>test[][], Test>>foo:[test-block0][], Test>>bar:[test-block0][], Test>>test-block0[][99]
		0005:  dbg '<string>', 3:15             MainClass>>main[][], Test>>test[][], Test>>foo:[test-block0][], Test>>bar:[test-block0][], Test>>test-block0[][99]
		0012:  return                           MainClass>>main[][99]
		0027:  dbg '<string>', 12:0             MainClass>>main[][99]
		0034:  return
		 */
		String input =
			"class Test [\n" +
			"  test [\n" +
			"    self foo: [^99].\n" +	// pass block with "return from test" to foo:
			"  ]\n" +
			"  foo: blk [\n" +
			"    self bar: blk\n" +		// pass that same block along to bar:
			"  ]\n" +
			"  bar: blk [\n" +
			"    blk value.\n" +		// eval block, forcing return from test;
										// unwind stack out of bar, foo and then return from test
			"  ]\n" +
			"]\n" +
			"^Test new test";
		String expecting = "99";
		execAndCheck(input, expecting);
	}
}
