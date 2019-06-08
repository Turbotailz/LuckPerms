/*
 * This file is part of LuckPerms, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package me.lucko.luckperms.common.api;

import me.lucko.luckperms.api.LuckPerms;
import me.lucko.luckperms.api.actionlog.ActionLogger;
import me.lucko.luckperms.api.context.ContextManager;
import me.lucko.luckperms.api.context.ContextSetFactory;
import me.lucko.luckperms.api.event.EventBus;
import me.lucko.luckperms.api.messaging.MessagingService;
import me.lucko.luckperms.api.messenger.MessengerProvider;
import me.lucko.luckperms.api.metastacking.MetaStackFactory;
import me.lucko.luckperms.api.model.group.GroupManager;
import me.lucko.luckperms.api.model.user.UserManager;
import me.lucko.luckperms.api.node.NodeBuilderRegistry;
import me.lucko.luckperms.api.platform.PlatformInfo;
import me.lucko.luckperms.api.track.TrackManager;
import me.lucko.luckperms.common.api.implementation.ApiActionLogger;
import me.lucko.luckperms.common.api.implementation.ApiContextManager;
import me.lucko.luckperms.common.api.implementation.ApiContextSetFactory;
import me.lucko.luckperms.common.api.implementation.ApiGroupManager;
import me.lucko.luckperms.common.api.implementation.ApiMessagingService;
import me.lucko.luckperms.common.api.implementation.ApiMetaStackFactory;
import me.lucko.luckperms.common.api.implementation.ApiNodeBuilderRegistry;
import me.lucko.luckperms.common.api.implementation.ApiPlatformInfo;
import me.lucko.luckperms.common.api.implementation.ApiTrackManager;
import me.lucko.luckperms.common.api.implementation.ApiUserManager;
import me.lucko.luckperms.common.config.ConfigKeys;
import me.lucko.luckperms.common.messaging.LuckPermsMessagingService;
import me.lucko.luckperms.common.plugin.LuckPermsPlugin;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Implements the LuckPerms API using the plugin instance
 */
public class LuckPermsApiProvider implements LuckPerms {

    private final LuckPermsPlugin plugin;

    private final PlatformInfo platformInfo;
    private final UserManager userManager;
    private final GroupManager groupManager;
    private final TrackManager trackManager;
    private final ActionLogger actionLogger;
    private final ContextManager contextManager;
    private final MetaStackFactory metaStackFactory;

    public LuckPermsApiProvider(LuckPermsPlugin plugin) {
        this.plugin = plugin;

        this.platformInfo = new ApiPlatformInfo(plugin);
        this.userManager = new ApiUserManager(plugin, plugin.getUserManager());
        this.groupManager = new ApiGroupManager(plugin, plugin.getGroupManager());
        this.trackManager = new ApiTrackManager(plugin, plugin.getTrackManager());
        this.actionLogger = new ApiActionLogger(plugin);
        this.contextManager = new ApiContextManager(plugin, plugin.getContextManager());
        this.metaStackFactory = new ApiMetaStackFactory(plugin);
    }

    @Override
    public String getServerName() {
        return this.plugin.getConfiguration().get(ConfigKeys.SERVER);
    }

    @Override
    public ContextSetFactory getContextSetFactory() {
        return ApiContextSetFactory.INSTANCE;
    }

    @Override
    public @NonNull PlatformInfo getPlatformInfo() {
        return this.platformInfo;
    }

    @Override
    public @NonNull UserManager getUserManager() {
        return this.userManager;
    }

    @Override
    public @NonNull GroupManager getGroupManager() {
        return this.groupManager;
    }

    @Override
    public @NonNull TrackManager getTrackManager() {
        return this.trackManager;
    }

    @Override
    public @NonNull CompletableFuture<Void> runUpdateTask() {
        return this.plugin.getSyncTaskBuffer().request();
    }

    @Override
    public @NonNull EventBus getEventBus() {
        return this.plugin.getEventFactory().getEventBus();
    }

    @Override
    public @NonNull Optional<MessagingService> getMessagingService() {
        return this.plugin.getMessagingService().map(ApiMessagingService::new);
    }

    @Override
    public void registerMessengerProvider(@NonNull MessengerProvider messengerProvider) {
        if (this.plugin.getConfiguration().get(ConfigKeys.MESSAGING_SERVICE).equals("custom")) {
            this.plugin.setMessagingService(new LuckPermsMessagingService(this.plugin, messengerProvider));
        }
    }

    @Override
    public @NonNull ActionLogger getActionLogger() {
        return this.actionLogger;
    }

    @Override
    public @NonNull ContextManager getContextManager() {
        return this.contextManager;
    }

    @Override
    public @NonNull Collection<String> getKnownPermissions() {
        return this.plugin.getPermissionRegistry().rootAsList();
    }

    @Override
    public @NonNull NodeBuilderRegistry getNodeBuilderRegistry() {
        return ApiNodeBuilderRegistry.INSTANCE;
    }

    @Override
    public @NonNull MetaStackFactory getMetaStackFactory() {
        return this.metaStackFactory;
    }

}
