package javax.microedition.rms;
import com.ptribes.newhorizons.Host;
import java.io.*;
import java.util.ArrayList;
public class RecordStore {
    private final String name; private final ArrayList<byte[]> recs = new ArrayList<byte[]>();
    private RecordStore(String n) { name = n; }
    private static File file(String n) { File d = new File(Host.ctx.getFilesDir(), "rms"); d.mkdirs(); return new File(d, n.replaceAll("[^A-Za-z0-9_.-]", "_") + ".rs"); }
    public static synchronized RecordStore openRecordStore(String n, boolean create) throws RecordStoreException {
        File f = file(n); RecordStore rs = new RecordStore(n);
        if (f.exists()) {
            try { DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(f)));
                int c = in.readInt(); for (int i = 0; i < c; i++) { int l = in.readInt(); if (l < 0) rs.recs.add(null); else { byte[] b = new byte[l]; in.readFully(b); rs.recs.add(b); } }
                in.close();
            } catch (IOException e) { throw new RecordStoreException(e.toString()); }
        } else if (!create) throw new RecordStoreNotFoundException(n);
        else rs.save();
        return rs;
    }
    private synchronized void save() throws RecordStoreException {
        try { DataOutputStream o = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file(name))));
            o.writeInt(recs.size()); for (byte[] b : recs) { if (b == null) o.writeInt(-1); else { o.writeInt(b.length); o.write(b); } }
            o.close();
        } catch (IOException e) { throw new RecordStoreException(e.toString()); }
    }
    public void closeRecordStore() throws RecordStoreException { save(); }
    public static synchronized void deleteRecordStore(String n) throws RecordStoreException {
        File f = file(n); if (!f.exists()) throw new RecordStoreNotFoundException(n); f.delete();
    }
    public synchronized int addRecord(byte[] d, int off, int len) throws RecordStoreException {
        byte[] b = new byte[len]; System.arraycopy(d, off, b, 0, len); recs.add(b); save(); return recs.size();
    }
    public synchronized byte[] getRecord(int id) throws RecordStoreException {
        if (id < 1 || id > recs.size() || recs.get(id - 1) == null) throw new InvalidRecordIDException(String.valueOf(id));
        return recs.get(id - 1).clone();
    }
    public synchronized void setRecord(int id, byte[] d, int off, int len) throws RecordStoreException {
        if (id < 1 || id > recs.size()) throw new InvalidRecordIDException(String.valueOf(id));
        byte[] b = new byte[len]; System.arraycopy(d, off, b, 0, len); recs.set(id - 1, b); save();
    }
    public synchronized void deleteRecord(int id) throws RecordStoreException {
        if (id < 1 || id > recs.size()) throw new InvalidRecordIDException(String.valueOf(id)); recs.set(id - 1, null); save();
    }
    public synchronized int getNumRecords() { int n = 0; for (byte[] b : recs) if (b != null) n++; return n; }
    public int getNextRecordID() { return recs.size() + 1; }
    public String getName() { return name; }
}
