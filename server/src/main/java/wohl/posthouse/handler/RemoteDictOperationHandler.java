package wohl.posthouse.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.log4j.Log4j2;
import wohl.posthouse.context.JentitiContext;
import wohl.posthouse.store.PosthouseConfigStore;
import wohl.posthouse.store.RemoteDictStore;
import wohl.posthouse.util.Delimiter;
import wohl.posthouse.util.impl.LocalDataPersistenceProcessor;
import wohl.posthouse.util.intf.DataPersistenceProcessor;
import wohl.posthouse.util.intf.RemoteDictInstructionAnalyser;

@Log4j2
public class RemoteDictOperationHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        String msgStr = (String) msg;
        RemoteDictInstructionAnalyser remoteDictInstructionAnalyser = (RemoteDictInstructionAnalyser) JentitiContext.ctx().get("remoteDictInstructionAnalyser");
        DataPersistenceProcessor localDataPersistenceProcessor = (DataPersistenceProcessor) JentitiContext.ctx().get(LocalDataPersistenceProcessor.class);

        // data persistence if enabled
        if (PosthouseConfigStore.POSTHOUSE_CONFIG.getServer().getPersistence().isEnable())
            localDataPersistenceProcessor.append(Delimiter.getBodyStart() + msgStr.split(Delimiter.getBodyStart(), 2)[1]);

        if(remoteDictInstructionAnalyser.exec(msgStr)) {
            ctx.writeAndFlush(":)");
            log.info("Response=:) OK");
        }
        else {
            ctx.writeAndFlush(":(");
            log.warn("Response=:( ERR");
        }
        log.debug("key="+ RemoteDictStore.keySet);
        log.debug("expiring_key="+RemoteDictStore.keyExpMap);
        log.debug("string="+RemoteDictStore.stringMap);
        log.debug("set="+RemoteDictStore.setMap);
        log.debug("deque="+RemoteDictStore.dequeMap);
        log.debug("hash="+RemoteDictStore.hashMap);
        ctx.fireChannelRead(msg);
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.warn("Unexpected connection loss: " + ctx.channel().remoteAddress());
    }
}
