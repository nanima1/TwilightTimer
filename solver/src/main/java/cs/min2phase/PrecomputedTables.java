package cs.min2phase;

import java.io.IOException;

/** Loads the standard {@link Tools#saveTo} payload with allocation-free array decoding. */
public final class PrecomputedTables {

    public static final int TABLE_SIZE_BYTES = 997_738;

    private PrecomputedTables() {}

    public static void initFrom(byte[] data) throws IOException {
        if (data.length != TABLE_SIZE_BYTES) {
            throw new IOException(
                "Unexpected table size: " + data.length + ", expected " + TABLE_SIZE_BYTES
            );
        }

        // Search.init() takes these locks in the same order. Holding both prevents a solve
        // from observing partially decoded pruning tables.
        synchronized (Search.class) {
            synchronized (CoordCube.class) {
                if (Search.inited && CoordCube.initLevel == 2) {
                    return;
                }

                CubieCube.initMove();
                CubieCube.initSym();

                Decoder input = new Decoder(data);
                read(CubieCube.FlipS2R, input);
                read(CubieCube.TwistS2R, input);
                read(CubieCube.EPermS2R, input);
                read(CubieCube.FlipR2S, input);
                read(CubieCube.TwistR2S, input);
                read(CubieCube.EPermR2S, input);
                input.read(CubieCube.Perm2CombP);
                input.read(CubieCube.MPermInv);
                read(CubieCube.PermInvEdgeSym, input);

                read(CoordCube.UDSliceMove, input);
                read(CoordCube.TwistMove, input);
                read(CoordCube.FlipMove, input);
                read(CoordCube.UDSliceConj, input);
                read(CoordCube.UDSliceTwistPrun, input);
                read(CoordCube.UDSliceFlipPrun, input);
                read(CoordCube.CPermMove, input);
                read(CoordCube.EPermMove, input);
                read(CoordCube.MPermMove, input);
                read(CoordCube.MPermConj, input);
                read(CoordCube.CCombPConj, input);
                read(CoordCube.MCPermPrun, input);
                read(CoordCube.EPermCCombPPrun, input);

                if (Search.USE_TWIST_FLIP_PRUN) {
                    read(CubieCube.FlipS2RF, input);
                    read(CoordCube.TwistFlipPrun, input);
                }
                if (!input.isExhausted()) {
                    throw new IOException("Precomputed table layout did not consume the resource.");
                }

                Search.inited = true;
                CoordCube.initLevel = 2;
            }
        }
    }

    private static void read(char[] target, Decoder source) {
        source.read(target);
    }

    private static void read(int[] target, Decoder source) {
        source.read(target);
    }

    private static void read(char[][] target, Decoder source) {
        for (char[] row : target) {
            source.read(row);
        }
    }

    private static void read(int[][] target, Decoder source) {
        for (int[] row : target) {
            source.read(row);
        }
    }

    private static final class Decoder {
        private final byte[] data;
        private int position;

        private Decoder(byte[] data) {
            this.data = data;
        }

        private void read(byte[] target) {
            System.arraycopy(data, position, target, 0, target.length);
            position += target.length;
        }

        private void read(char[] target) {
            for (int i = 0; i < target.length; i++) {
                target[i] = (char) (
                    (data[position] & 0xff) << 8 |
                    (data[position + 1] & 0xff)
                );
                position += Character.BYTES;
            }
        }

        private void read(int[] target) {
            for (int i = 0; i < target.length; i++) {
                target[i] =
                    (data[position] & 0xff) << 24 |
                    (data[position + 1] & 0xff) << 16 |
                    (data[position + 2] & 0xff) << 8 |
                    (data[position + 3] & 0xff);
                position += Integer.BYTES;
            }
        }

        private boolean isExhausted() {
            return position == data.length;
        }
    }
}
