package com.seeker.luckyble.upgrade;

import androidx.annotation.NonNull;

import java.io.IOException;

/**
 * @author Seeker
 * @date 2018/4/25/025  17:33
 * @describe 升级信息
 */

class DataProvider {

    private static final int OAD_BLOCK_SIZE = 16;
    private static final int HAL_FLASH_WORD_SIZE = 4;
    private static final int OAD_IMG_HDR_SIZE = 8;
    private static final int OAD_BUFFER_SIZE = 2 + OAD_BLOCK_SIZE;

    private final ProgInfo mProgInfo = new ProgInfo();
    private final ImgHdr mFileImgHdr = new ImgHdr();
    private final ImgHdr mTargImgHdr = new ImgHdr();

    private final byte[] mOadBuffer = new byte[OAD_BUFFER_SIZE];

    private byte[] mFileBuffer;

    private boolean prepared = false;

    public DataProvider(){

    }

    /**
     * 设置升级文件来源
     * @param loader
     */
    public boolean setSourceBinLoader(@NonNull Loader loader){
        try {
            mFileBuffer = loader.loadFile();
            prepared = true;
        } catch (IOException e) {
            e.printStackTrace();
            prepared = false;
        }
        checkVersion();
        return prepared;
    }

    public void reset(){
        mProgInfo.reset();
    }

    public byte[] getIdentifyValues(){
        byte[] buf = new byte[OAD_IMG_HDR_SIZE + 2 + 2];
        buf[0] = ByteHelper.loUint16(mFileImgHdr.ver);
        buf[1] = ByteHelper.hiUint16(mFileImgHdr.ver);
        buf[2] = ByteHelper.loUint16(mFileImgHdr.len);
        buf[3] = ByteHelper.hiUint16(mFileImgHdr.len);
        System.arraycopy(mFileImgHdr.uid, 0, buf, 4, 4);
        return buf;
    }

    public byte[] getBlockValues(){
        if (mProgInfo.getIBlocksInt() < mProgInfo.getNBlocksInt()) {
            mOadBuffer[0] = ByteHelper.loUint16(mProgInfo.iBlocks);
            mOadBuffer[1] = ByteHelper.hiUint16(mProgInfo.iBlocks);
            System.arraycopy(mFileBuffer, mProgInfo.iBytes, mOadBuffer, 2, OAD_BLOCK_SIZE);
            mProgInfo.iBlocks++;
            mProgInfo.iBytes += OAD_BLOCK_SIZE;
            return mOadBuffer;
        }
        return null;
    }

    public float getBlockProgress(short index){
        return 1f*index / mProgInfo.nBlocks;
    }

    /**
     * 可能由于notify返回数据，判断写入失败，重置block的index
     */
    public void resetBlockIndex(short index){
        mProgInfo.iBlocks = index;
        mProgInfo.iBytes = index * OAD_BLOCK_SIZE;
    }

    /**
     * 是否发送block数据完成
     * @return
     */
    public boolean isWriterFinished(){
        return mProgInfo.getIBlocksInt()-1 >= mProgInfo.getNBlocksInt()-1;
    }

    /**
     * 是否升级完毕，当最后几组byte数据写入完成之后，并不一定会收到notify，因为写入完成之后，连接断开，最后收到的index是9853，总个数9856，最后索引值9855
     * 所以偏移5个算是完成
     * @param index
     * @return
     */
    public boolean isUpgradeFinished(short index){
        return Math.abs((index & 0xffff) - mProgInfo.getNBlocksInt()) < 5;
    }

    public boolean isNotifyChangedComplete(short index){
        return (index & 0xffff) == mProgInfo.getNBlocksInt();
    }

    /**
     * 版本校验
     */
    private void checkVersion(){
        if (!prepared){
            return;
        }
        mFileImgHdr.ver = ByteHelper.buildUint16(mFileBuffer[5], mFileBuffer[4]);
        mFileImgHdr.len = ByteHelper.buildUint16(mFileBuffer[7], mFileBuffer[6]);
        mFileImgHdr.imgType = ((mFileImgHdr.ver & 1) == 1) ? 'B' : 'A';
        System.arraycopy(mFileBuffer, 8, mFileImgHdr.uid, 0, 4);
        // Verify image types
        boolean ready = mFileImgHdr.imgType != mTargImgHdr.imgType;
    }


    private class ImgHdr {
        short ver;
        short len;
        Character imgType;
        byte[] uid = new byte[4];
    }

    private class ProgInfo {
        int iBytes = 0; // Number of bytes programmed
        short iBlocks = 0; // Number of blocks programmed
        short nBlocks = 0; // Total number of blocks
        int iTimeElapsed = 0; // Time elapsed in milliseconds

        void reset() {
            iBytes = 0;
            iBlocks = 0;
            iTimeElapsed = 0;
            nBlocks = (short) ((mFileImgHdr.len & 0xffff) / (OAD_BLOCK_SIZE / HAL_FLASH_WORD_SIZE));
        }

        private int getIBlocksInt() {
            return iBlocks & 0xffff;
        }

        private int getNBlocksInt() {
            return nBlocks & 0xffff;
        }
    }

}
