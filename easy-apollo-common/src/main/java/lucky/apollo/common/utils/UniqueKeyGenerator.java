package lucky.apollo.common.utils;

import com.google.common.base.Joiner;
import org.apache.commons.lang.time.FastDateFormat;

import java.security.SecureRandom;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author luckylau
 * @Date 2019/7/12
 */
public class UniqueKeyGenerator {
    private static final FastDateFormat TIMESTAMP_FORMAT = FastDateFormat.getInstance("yyyyMMddHHmmss");
    private static final AtomicInteger counter = new AtomicInteger(new SecureRandom().nextInt());
    private static final Joiner KEY_JOINER = Joiner.on("-");

    private static final char[] HEX_CHARS = new char[]{
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private static byte int3(final int x) {
        return (byte) (x >> 24);
    }

    private static byte int2(final int x) {
        return (byte) (x >> 16);
    }

    private static byte int1(final int x) {
        return (byte) (x >> 8);
    }

    private static byte int0(final int x) {
        return (byte) (x);
    }

    private static String toHexString(byte[] bytes) {
        char[] chars = new char[bytes.length * 2];
        int i = 0;
        for (byte b : bytes) {
            chars[i++] = HEX_CHARS[b >> 4 & 0xF];
            chars[i++] = HEX_CHARS[b & 0xF];
        }
        return new String(chars);
    }

    /**
     * Concat machine id, counter and key to byte array
     * Only retrieve lower 3 bytes of the id and counter and 2 bytes of the keyHashCode
     */
    private static byte[] toByteArray(int keyHashCode, int machineIdentifier, int counter) {
        byte[] bytes = new byte[8];
        bytes[0] = int1(keyHashCode);
        bytes[1] = int0(keyHashCode);
        bytes[2] = int2(machineIdentifier);
        bytes[3] = int1(machineIdentifier);
        bytes[4] = int0(machineIdentifier);
        bytes[5] = int2(counter);
        bytes[6] = int1(counter);
        bytes[7] = int0(counter);
        return bytes;
    }

    public static String generate(Object... args) {
        String hexIdString =
                toHexString(toByteArray(Objects.hash(args), MachineUtil.getMachineIdentifier(),
                        counter.incrementAndGet()));

        return KEY_JOINER.join(TIMESTAMP_FORMAT.format(new Date()), hexIdString);

    }
}